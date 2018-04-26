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
*�����������ƿ���Ų�ǰ�Ķ��������Ϲ�
* 
*1.platingOrderDivide()�Ƕ�����֣����ݿ��϶�Ӧ���ǽ����Wopת��ΪJobItem�Ĺ���
*2.combine()�ǺϹң����ݿ��϶�Ӧ���ǽ�JobItem�Ϲ�ΪJob�Ĺ���,����洢�����ݿ�
*3.divideAndCombine()�ǲ����Ϲҵ�һ��������̣������platingOrderDivide()��combine()���������һ�������ݿ���Ӧ����Ƿ�Ϊ������
*���Ƿ���Ҫ�洢�����ݿ�
*
*����ʹ�ø���ʱ�����Ƚ�jobitem��job����գ���
*/
public class BeforeBottleneck extends BottleneckBasic {
	Service service = Service.getService();
	
	public static void main(String[] args) {
		BeforeBottleneck ab = new BeforeBottleneck();
		ab.divideAndCombine();
		HibernateUtils.closeAll();
		System.out.println("���������Ϲ������");
	}
	
	public List<Job> divideAndCombine(){
		//�ҵ����еĵ��wop
		Process platingProcess =  service.getProcessByName("���");
		@SuppressWarnings("unchecked")
		List<Wop> platingWop = service.list(Wop.class,"process",platingProcess);
		
		//���
		List<JobItem> jobItems = platingOrderDivide(platingWop);
		//������ݿ�jobitem���Ϊ�գ������ǵ�һ��ִ�У��Ὣ����־û��������ݱ��浽���ݿ�
		if(service.list(JobItem.class).isEmpty()) {
			service.save(jobItems);
		}
		
		//�Ϲ�
		List<Job> jobs = combine();
		
		return jobs;
	}
	
	//�����еĵ��JobItemת����Job
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
	
	//�����ĵ��itemsֱ��ת����job
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
	
	//��ȫ���Ƿ����ĵ��items�ϲ�
	public List<Job> combineNotFull(List<JobItem> notFullItems){
		List<Job> jobs = new LinkedList<>();
		
		//�Ƚ�items�����ʱ�����ִ���map��keyֵΪ���ʱ�䣬valueΪitem��List����
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
		//�ֱ��ÿ�ֲ�ͬ���ʱ���List���Ͻ��кϹ�
		for (Entry<Double, List<JobItem>> entry : entrySet) {
			List<JobItem> partItems = entry.getValue();
			
			//ѭ����ֱ����List����Ϊ��
			while(!partItems.isEmpty()) {
				Job job = new Job();
				JobItem firstItem = partItems.remove(0);
				if(job.getJobItems()==null) {
					List<JobItem> jobitemsOfjob = new LinkedList<>();
					job.setJobItems(jobitemsOfjob);
				}
				job.getJobItems().add(firstItem);			//�Ƚ�List���еĵ�һ��Ԫ�ؼ���job
				job.setProcessTime(firstItem.getWop().getProcessTime());
				double currentLength = firstItem.getWop().getWidth()*firstItem.getQty();
				for(int i=0;i<partItems.size();) {				//����Ѱ�ң�ֻҪ����Ϲ����������
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
	
	//�����еĶ�����ֳ�jobItem
	public List<JobItem> platingOrderDivide(List<Wop> platingWop) {
		
		List<JobItem> list = new LinkedList<>();
		for (Wop wop : platingWop) {
			list.addAll(platingSingleOrderDivide(wop));
		}
			
		return list;
	}
	
	//������wop��ֳ�jobItem
	public List<JobItem> platingSingleOrderDivide(Wop wop){
		
		Process platingProcess =  service.getProcessByName("���");
		//��������ǵ�ƣ�ֱ�ӷ��ؿ�
		if(!wop.getProcess().equals(platingProcess))
			return null;
		
		int qtyPerHang = (int)(ResourceUtils.getBarLength()/wop.getWidth()); //����ÿ�ҷɰͿ������ҵİ���
		
		boolean hasRemain = (wop.getQty()%qtyPerHang!=0);	//�ж��Ƿ�ʣ�����
		int hangNum = (hasRemain?(wop.getQty()/qtyPerHang+1):(wop.getQty()/qtyPerHang));	//�����ܹ���
		
		List<JobItem> list = new LinkedList<>();
		//ѭ���������Ҽ���list
		for(int i = 0;i<hangNum-1;i++) {
			JobItem jobItem = new JobItem();
			jobItem.setOrder(wop.getOrder());
			jobItem.setQty(qtyPerHang);
			jobItem.setWop(wop);
			jobItem.setIsfull(true);
			list.add(jobItem);
		}
		
		int remain = wop.getQty()-qtyPerHang*(hangNum-1);	//ʣ��İ���
		
		JobItem jobItem = new JobItem();
		jobItem.setOrder(wop.getOrder());
		jobItem.setQty(remain);
		jobItem.setWop(wop);
		jobItem.setIsfull(!hasRemain);
		list.add(jobItem);
		
		return list;
	}
}
