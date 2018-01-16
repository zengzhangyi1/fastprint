package cn.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtils {
	private static Configuration cfg =null;
	private static SessionFactory sessionFactory =null;
	private static Session session = null;
	
	static {
		cfg = new Configuration();
		cfg.configure();
	}
	
//	public static SessionFactory getSessionFactory() {
//		return sessionFactory;
//	}
	public static Session getSession() {
		sessionFactory = cfg.buildSessionFactory();
		session = sessionFactory.openSession();
		return session;
	}
	
	public static void closeAll() {
		session.close();
		sessionFactory.close();
	}
}
