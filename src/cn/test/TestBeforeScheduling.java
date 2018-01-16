package cn.test;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;

import cn.bottleneck.BeforeScheduling;
import cn.pojo.Job;
import cn.pojo.JobItem;
import cn.pojo.Wop;
import cn.service.Service;
import cn.utils.HibernateUtils;


public class TestBeforeScheduling {
	@Test
	public void TestPlatingSingleOrderDivide() {
		
		Session session = HibernateUtils.getSession();
		
		Transaction tx = session.beginTransaction();
		
		Query query = session.createQuery("from Wop");
		List<Wop> list = query.list();
		
		BeforeScheduling run = new BeforeScheduling();
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
		
		BeforeScheduling bs = new BeforeScheduling();
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
		
		BeforeScheduling bs = new BeforeScheduling();
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
		BeforeScheduling bs = new BeforeScheduling();
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
		BeforeScheduling bs = new BeforeScheduling();
		
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
