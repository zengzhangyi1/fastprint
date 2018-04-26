package cn.test;

import java.util.List;

import org.hibernate.Session;
import org.junit.Test;

import cn.pojo.Job;
import cn.pojo.JobItem;
import cn.pojo.Wop;
import cn.service.Service;
import cn.utils.HibernateUtils;

public class TestHibernate {
	@Test
	public void TestgetProcessId() {
		Service service = Service.getService();
		cn.pojo.Process result = service.getProcessByName("µç¶Æ");
        System.out.println(result);
        HibernateUtils.closeAll();
	}
	
	@Test
	public void TestList() {
		Service service = Service.getService();
		cn.pojo.Process elec = service.getProcessByName("µç¶Æ");
		List<Wop> electricWop = service.list(Wop.class, "process",elec);
		for (Wop wop : electricWop) {
			System.out.println(wop);
		}
		System.out.println(electricWop.size());
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestClear() {
		Service service = Service.getService();
		service.clearTable(Job.class);
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestUpdate() {
		Service service = Service.getService();
		Job job = new Job();
		job.setJobId(1);
		job.setProcessTime(1.3d);
		service.update(job);
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestUpdate2() {
		Session session = HibernateUtils.getSession();
		
		Job job = session.get(Job.class, 1);
		job.setProcessTime(1.3d);
		session.update(job);
		session.flush();
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestRemoveJob() {
		Service service = Service.getService();
		List<JobItem> jobItems = service.list(JobItem.class);
		service.removeJob(jobItems);
		HibernateUtils.closeAll();
	}
}
