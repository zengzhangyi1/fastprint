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
 * �����������Ų�������ݿ�洢��������Ų���ɵ�jobתΪtask
 * 
 *job2Task()�ǽ�����Ų���ɺ�job������ת����task�����ݣ���2017-1-1 8:00Ϊ��㡣job��������0ʱ��Ϊ��㣬����Ϊ��λ���ϹҺ��Ų�
*�ĵ�λ��jobitem�ǲ�ֺ󣬺Ϲ�ǰ�ĵ�λ��һ��wop���ж��jobitem��ÿ��wop�У�����jobitem�����翪ʼʱ����Ϊwop�Ŀ�ʼʱ�䣬jobitem��
*��Ľ���ʱ����Ϊwop�Ľ���ʱ��
*
*/
public class AfterBottleneck extends BottleneckBasic {
	public static void main(String[] args) {
		AfterBottleneck run = new AfterBottleneck();
		run.job2Task();
		System.out.println("���");
		HibernateUtils.closeAll();
	}
	//��ɴ����ݿ���job��Task��ת��
	@SuppressWarnings("unchecked")
	public void job2Task() {
		List<Task> tasks = new ArrayList<>();
		//�õ����е�Ƶ�wop
		cn.pojo.Process eletricplate = service.getProcessByName("���");
		List<Wop> electricWop = service.list(Wop.class, "process",eletricplate);
		
		//��ÿһ��wop����ת����task
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
