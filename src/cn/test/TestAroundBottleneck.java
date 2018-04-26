package cn.test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;

import cn.bottleneck.AfterBottleneck;
import cn.bottleneck.BeforeBottleneck;
import cn.pojo.Job;
import cn.pojo.JobItem;
import cn.pojo.Task;
import cn.pojo.Wop;
import cn.pojo.Process;
import cn.service.Service;
import cn.utils.HibernateUtils;


public class TestAroundBottleneck {
	@Test
	public void TestSaveTask() {
		Service service = Service.getService();
		Task task = new Task();
		LocalDateTime ldt = LocalDateTime.now();
		task.setEndTime(ldt);
		service.save(task);
		HibernateUtils.closeAll();
	}
	
	@Test
	public void Testjob2Task() {
		AfterBottleneck run = new AfterBottleneck();
		run.job2Task();
		HibernateUtils.closeAll();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void TestPlatingSingleOrderDivide() {
		
		Session session = HibernateUtils.getSession();
		
		Transaction tx = session.beginTransaction();
		
		Query query = session.createQuery("from Wop");
		List<Wop> list = query.list();
		
		BeforeBottleneck run = new BeforeBottleneck();
		List<JobItem> items= run.platingSingleOrderDivide(list.get(1));
		
		for (JobItem jobItem : items) {
			System.out.println(jobItem);
		}
		
        tx.commit();
        
        HibernateUtils.closeAll();
	}
	
	
	@Test
	public void TestCombineNotFull() {
		Session session = HibernateUtils.getSession();
		Criteria criteria = session.createCriteria(JobItem.class);
		criteria.add(Restrictions.eq("isfull", false));
		List<JobItem> notFullItems = criteria.list();
		
		BeforeBottleneck bs = new BeforeBottleneck();
		List<Job> jobs = bs.combineNotFull(notFullItems);
		
		int jobItemSize=0;
		for(int i=0 ; i<jobs.size();i++) {
			System.out.println("-------"+i+"----------");
			jobItemSize += jobs.get(i).getJobItems().size();
			for(JobItem item:jobs.get(i).getJobItems()) {
				System.out.println(item);
			}
		}
		System.out.println("jobItemSize:"+jobItemSize);
		
        HibernateUtils.closeAll();
	}
	
	@Test
	public void TestCombineFull() {
		Session session = HibernateUtils.getSession();
		Criteria criteria = session.createCriteria(JobItem.class);
		criteria.add(Restrictions.eq("isfull", true));
		List<JobItem> fullItems = criteria.list();
		
		BeforeBottleneck bs = new BeforeBottleneck();
		List<Job> jobs = bs.combineFull(fullItems);
		
		int jobItemSize=0;
		for(int i=0 ; i<jobs.size();i++) {
			System.out.println("-------"+i+"----------");
			jobItemSize += jobs.get(i).getJobItems().size();
			for(JobItem item:jobs.get(i).getJobItems()) {
				System.out.println(item);
			}
		}
		System.out.println("jobItemSize:"+jobItemSize);
		
        HibernateUtils.closeAll();
	}
	
	@Test
	public void TestCombine() {
		BeforeBottleneck bs = new BeforeBottleneck();
		List<Job> jobs  = bs.combine();
		
		int jobItemSize=0;
		for(int i=0 ; i<jobs.size();i++) {
			System.out.println("-------"+i+"----------");
			jobItemSize += jobs.get(i).getJobItems().size();
			for(JobItem item:jobs.get(i).getJobItems()) {
				System.out.println(item);
			}
		}
		System.out.println("jobItemSize:"+jobItemSize);
		
        HibernateUtils.closeAll();
	}
	
	@Test
	public void TestDivideAndCombine() {
		BeforeBottleneck bs = new BeforeBottleneck();
		
		List<Job> jobs  = bs.divideAndCombine();
		
		int jobItemSize=0;
		for(int i=0 ; i<jobs.size();i++) {
			System.out.println("-------"+i+"----------");
			jobItemSize += jobs.get(i).getJobItems().size();
			for(JobItem item:jobs.get(i).getJobItems()) {
				System.out.println(item);
			}
		}
		System.out.println("jobItemSize:"+jobItemSize);
		
        HibernateUtils.closeAll();
	}
	
	
}	
