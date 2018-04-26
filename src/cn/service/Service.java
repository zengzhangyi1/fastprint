package cn.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import cn.pojo.JobItem;
import cn.pojo.Process;
import cn.utils.HibernateUtils;

@SuppressWarnings("rawtypes")
public class Service {
	private static Service service = new Service();
	private Service() {
		
	}
	public static Service getService() {
		return service;
	}
	
	private static Session session = HibernateUtils.getSession();

	
	public List list(Class clazz) {
		Criteria criteria = session.createCriteria(clazz);
		return criteria.list();
	}
	
	public List list(Class clazz,Object... pairParms) {
		HashMap<String,Object> map = new HashMap<>();
		for(int i = 0;i<pairParms.length;i+=2) {
			map.put(pairParms[i].toString(), pairParms[i+1]);
		}
		
		Criteria criteria = session.createCriteria(clazz);
		
		Set<String> keyset = map.keySet();
		for (String key : keyset) {
			if(null==map.get(key)) {
				criteria.add(Restrictions.isNull(key));
			}
			else {
				criteria.add(Restrictions.eq(key, map.get(key)));
			}
		}
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	public Process getProcessByName(String processName) {
		
		Criteria criteria = session.createCriteria(Process.class);
		criteria.add(Restrictions.eq("processName",processName));
		List<Process> list = criteria.list();
		if(!list.isEmpty()) {
			return list.get(0);
		}
		else return null;
	}
	
/*	public List<Wop> getWopByProcess(Process process) {
		
		Criteria criteria = session.createCriteria(Wop.class);
		criteria.add(Restrictions.eq("process", process));
		return criteria.list();
	}*/
	
	public Object get(Class<?> clazz,Serializable i) {
		return session.get(clazz,i);
	}
	
	public void save(Object obj) {
		session.save(obj);
	}
	
	public void save(List obj) {
		for (Object object : obj) {
			session.save(object);
		}
	}
	
	public void update(Object obj) {
		session.update(obj);
	}
	public void delete(Object obj) {
		session.delete(obj);
	}
	public void evict(Object obj) {
		session.evict(obj);
	}
	public void clearTable(Class clazz) {
		String className = clazz.getSimpleName();
		String hql = "delete "+className;
		Query query = session.createQuery(hql);
		query.executeUpdate();
	}
	
	public int count(Class clazz) {
		Criteria criteria = session.createCriteria(clazz);
		
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		
		return count.intValue();
	}
	
	public void flush() {
		session.flush();
	}
	
	public void removeJob(List<JobItem> jobItems) {
		for (JobItem jobItem : jobItems) {
			jobItem.setJob(null);
			update(jobItem);
		}
		session.flush();
	}
	
	public Transaction beginTransaction() {
		return session.beginTransaction();
	}
}
