package cn.test;

import java.util.List;

import org.hibernate.Session;
import org.junit.Test;

import cn.pojo.Job;
import cn.pojo.JobItem;
import cn.service.Service;
import cn.utils.HibernateUtils;

public class TestHibernate {
	@Test
	public void TestgetProcessId() {
		Service service = new Service();
		cn.pojo.Process result = service.getProcessByName("µç¶Æ");
        System.out.println(result);
        HibernateUtils.closeAll();
	}
	
	@Test
	public void TestList() {
		Service service = new Service();
		List<JobItem> notfullItems = service.list(JobItem.class,"isfull",false);
		for (JobItem jobItem : notfullItems) {
			System.out.println(jobItem);
		}
		System.out.println(notfullItems.size());
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestClear() {
		Service service = new Service();
		service.clearTable(Job.class);
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestUpdate() {
		Service service = new Service();
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
		Service service = new Service();
		List<JobItem> jobItems = service.list(JobItem.class);
		service.removeJob(jobItems);
		HibernateUtils.closeAll();
	}
}
