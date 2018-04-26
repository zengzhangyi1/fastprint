package cn.bottleneck;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Transaction;

import cn.pojo.Job;
import cn.pojo.JobItem;
import cn.pojo.Process;
import cn.pojo.Wop;
import cn.service.Service;
import cn.utils.HibernateUtils;
import cn.utils.ResourceUtils;


/*
*该类的作用是瓶颈排产前的订单拆分与合挂
* 
*1.platingOrderDivide()是订单拆分，数据库上对应的是将电镀Wop转换为JobItem的过程
*2.combine()是合挂，数据库上对应的是将JobItem合挂为Job的过程,并会存储到数据库
*3.divideAndCombine()是拆分与合挂的一个整体过程，会调用platingOrderDivide()，combine()函数，并且会根据数据库相应表格是否为空来判
*断是否需要存储到数据库
*
*！！使用该类时，需先将jobitem和job表清空！！
*/
public class BeforeBottleneck extends BottleneckBasic {
	Service service = Service.getService();
	
	public static void main(String[] args) {
		BeforeBottleneck ab = new BeforeBottleneck();
		ab.divideAndCombine();
		HibernateUtils.closeAll();
		System.out.println("订单拆分与合挂已完成");
	}
	
	public List<Job> divideAndCombine(){
		//找到所有的电镀wop
		Process platingProcess =  service.getProcessByName("电镀");
		@SuppressWarnings("unchecked")
		List<Wop> platingWop = service.list(Wop.class,"process",platingProcess);
		
		//拆分
		List<JobItem> jobItems = platingOrderDivide(platingWop);
		//如果数据库jobitem表格为空，表明是第一次执行，会将对象持久化，将数据保存到数据库
		if(service.list(JobItem.class).isEmpty()) {
			service.save(jobItems);
		}
		
		//合挂
		List<Job> jobs = combine();
		
		return jobs;
	}
	
	//将所有的电镀JobItem转换成Job
	@SuppressWarnings("unchecked")
	public List<Job> combine() {
		List<JobItem> fullItems = service.list(JobItem.class, "isfull",true);
		List<JobItem> notFullItems = service.list(JobItem.class, "isfull",false);
		List<Job> jobs = combineFull(fullItems);
		jobs.addAll(combineNotFull(notFullItems));
		
		if(service.count(Job.class)>0) {
			Transaction tx  = service.beginTransaction();
			service.removeJob(service.list(JobItem.class));
			service.clearTable(Job.class);
			tx.commit();
		}
		
		
		for (int i = 0;i<jobs.size();i++) {
			jobs.get(i).setJobId(i+1);
			Transaction tx = service.beginTransaction();
			if(service.get(Job.class, i+1)!=null) {
				service.evict(service.get(Job.class, i+1));
			}
			service.save(jobs.get(i));
			for(JobItem jobitem:jobs.get(i).getJobItems()) {
				jobitem.setJob(jobs.get(i));
				service.update(jobitem);
				service.flush();
			}
			tx.commit();
		}
		
		return jobs;
	}
	
	//将满的电镀items直接转换成job
	public List<Job> combineFull(List<JobItem> fullItems){
		List<Job> jobs = new LinkedList<>();
		for (JobItem jobitem : fullItems) {
			Job job = new Job();
			List<JobItem> itemlist = new LinkedList<>();
			itemlist.add(jobitem);
			job.setJobItems(itemlist);
			job.setProcessTime(jobitem.getWop().getProcessTime());
			jobs.add(job);
		}
		return jobs;
	}
	
	//对全部是非满的电镀items合并
	public List<Job> combineNotFull(List<JobItem> notFullItems){
		List<Job> jobs = new LinkedList<>();
		
		//先将items按电镀时间区分存入map，key值为电镀时间，value为item的List集合
		Map<Double,List<JobItem>> map = new HashMap<>();
		for(JobItem item:notFullItems) {
			double processTime = item.getWop().getProcessTime();
			if(map.containsKey(processTime)) {
				map.get(processTime).add(item);
			}
			else {
				List<JobItem> itemlist = new LinkedList<>();
				itemlist.add(item);
				map.put(processTime, itemlist);
			}
		}
		
		Set<Entry<Double, List<JobItem>>> entrySet = map.entrySet();
		//分别对每种不同电镀时间的List集合进行合挂
		for (Entry<Double, List<JobItem>> entry : entrySet) {
			List<JobItem> partItems = entry.getValue();
			
			//循环，直到该List集合为空
			while(!partItems.isEmpty()) {
				Job job = new Job();
				JobItem firstItem = partItems.remove(0);
				if(job.getJobItems()==null) {
					List<JobItem> jobitemsOfjob = new LinkedList<>();
					job.setJobItems(jobitemsOfjob);
				}
				job.getJobItems().add(firstItem);			//先将List当中的第一个元素加入job
				job.setProcessTime(firstItem.getWop().getProcessTime());
				double currentLength = firstItem.getWop().getWidth()*firstItem.getQty();
				for(int i=0;i<partItems.size();) {				//往后寻找，只要满足合挂条件便放入
					double itemLength = partItems.get(i).getWop().getWidth()*partItems.get(i).getQty();
					if(itemLength+currentLength<=ResourceUtils.getBarLength()&&
							partItems.get(i).getWop().getProcessTime()==firstItem.getWop().getProcessTime()) {
						job.getJobItems().add(partItems.remove(i));
						currentLength += itemLength;
					}
					else {
						i++;
					}
				}
				jobs.add(job);
			}
		}
		return jobs;
	}
	
	//将所有的订单拆分成jobItem
	public List<JobItem> platingOrderDivide(List<Wop> platingWop) {
		
		List<JobItem> list = new LinkedList<>();
		for (Wop wop : platingWop) {
			list.addAll(platingSingleOrderDivide(wop));
		}
			
		return list;
	}
	
	//将单个wop拆分成jobItem
	public List<JobItem> platingSingleOrderDivide(Wop wop){
		
		Process platingProcess =  service.getProcessByName("电镀");
		//如果工序不是电镀，直接返回空
		if(!wop.getProcess().equals(platingProcess))
			return null;
		
		int qtyPerHang = (int)(ResourceUtils.getBarLength()/wop.getWidth()); //计算每挂飞巴可以悬挂的板数
		
		boolean hasRemain = (wop.getQty()%qtyPerHang!=0);	//判断是否剩余余板
		int hangNum = (hasRemain?(wop.getQty()/qtyPerHang+1):(wop.getQty()/qtyPerHang));	//计算总挂数
		
		List<JobItem> list = new LinkedList<>();
		//循环将整数挂加入list
		for(int i = 0;i<hangNum-1;i++) {
			JobItem jobItem = new JobItem();
			jobItem.setOrder(wop.getOrder());
			jobItem.setQty(qtyPerHang);
			jobItem.setWop(wop);
			jobItem.setIsfull(true);
			list.add(jobItem);
		}
		
		int remain = wop.getQty()-qtyPerHang*(hangNum-1);	//剩余的板数
		
		JobItem jobItem = new JobItem();
		jobItem.setOrder(wop.getOrder());
		jobItem.setQty(remain);
		jobItem.setWop(wop);
		jobItem.setIsfull(!hasRemain);
		list.add(jobItem);
		
		return list;
	}
}
