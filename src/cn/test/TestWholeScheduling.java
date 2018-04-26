package cn.test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import cn.pojo.Order;
import cn.pojo.Process;
import cn.pojo.Wop;
import cn.service.Service;
import cn.utils.HibernateUtils;
import cn.wholeplan.WholeBasic;
import cn.wholeplan.WholeScheduling;

public class TestWholeScheduling {
	@Test
	public void TestPushPull() {
		WholeScheduling ws = new WholeScheduling();
		ws.pushPull();
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestPullByProcess() {
		WholeBasic wb  = new WholeBasic();
		wb.pullByProcess(5,true);
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestcaculateWopLET() {
		Service service = Service.getService();
		Process nowProcess = (Process) service.get(Process.class, 7);
		List<Wop> wopforProcess = service.list(Wop.class,"process",nowProcess);
		
		WholeBasic wb  = new WholeBasic();
		wb.caculateWopLET(wopforProcess, false);
		
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestPushByProcess() {
		WholeBasic wb  = new WholeBasic();
		wb.pushByProcess(15,true);
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestcaculateWopEST() {
		Service service = Service.getService();
		Process nowProcess = (Process) service.get(Process.class, 13);
		List<Wop> wopforProcess = service.list(Wop.class,"process",nowProcess);
		
		WholeBasic wb  = new WholeBasic();
		wb.caculateWopEST(wopforProcess, false);
		
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestWops() {
		Service service = Service.getService();
		
		List<Order> orders = service.list(Order.class);
		List<Wop> wops = service.list(Wop.class,"order",orders.get(0));
			
		//根据wopId对wops排序
		Collections.sort(wops, (o1,o2)->o1.getWopId().compareTo(o2.getWopId()));
		for (Wop wop : wops) {
			System.out.println(wop);
		}
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestLoacalDateTime() {
		LocalDateTime ldt1 = LocalDateTime.of(2017, 1, 1, 8, 0);
		LocalDateTime ldt2 = LocalDateTime.of(2017, 1, 1, 9, 0);
		long time = ldt2.until(ldt1, ChronoUnit.MINUTES);
		System.out.println(time);
	}
}
