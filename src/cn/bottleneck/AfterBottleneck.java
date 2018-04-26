package cn.bottleneck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Transaction;

import cn.pojo.JobItem;
import cn.pojo.Task;
import cn.pojo.Wop;
import cn.utils.HibernateUtils;
/*
 * 该类作用是排产后的数据库存储，将电镀排产完成的job转为task
 * 
 *job2Task()是将电镀排产完成后job的数据转换成task的数据，以2017-1-1 8:00为起点。job数据是以0时刻为起点，分钟为单位，合挂后排产
*的单位，jobitem是拆分后，合挂前的单位。一个wop含有多个jobitem，每个wop中，根据jobitem的最早开始时间作为wop的开始时间，jobitem最
*晚的结束时间作为wop的结束时间
*
*/
public class AfterBottleneck extends BottleneckBasic {
	public static void main(String[] args) {
		AfterBottleneck run = new AfterBottleneck();
		run.job2Task();
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	//完成从数据库里job到Task的转换
	@SuppressWarnings("unchecked")
	public void job2Task() {
		List<Task> tasks = new ArrayList<>();
		//得到所有电镀的wop
		cn.pojo.Process eletricplate = service.getProcessByName("电镀");
		List<Wop> electricWop = service.list(Wop.class, "process",eletricplate);
		
		//将每一个wop依次转换成task
		for (Wop wop : electricWop) {
			List<JobItem> jobItems = service.list(JobItem.class,"wop",wop);
			double startTime=Double.MAX_VALUE,endTime=0;
			for (JobItem jobItem : jobItems) {
				startTime = Math.min(jobItem.getJob().getStartTime(), startTime);
				endTime = Math.max(jobItem.getJob().getEndTime(), endTime);
			}
			Task task = new Task();
			LocalDateTime startldt = zeroTime.plusMinutes((long) startTime);
			LocalDateTime endldt = zeroTime.plusMinutes((long) endTime);
			task.setResuorceId(1);
			task.setStartTime(startldt);
			task.setEndTime(endldt);
			task.setWop(wop);
			task.setOrder(wop.getOrder());
			task.setProcess(wop.getProcess());
			tasks.add(task);
		}
		Transaction tx = service.beginTransaction();
		service.save(tasks);
		tx.commit();
	}
}
