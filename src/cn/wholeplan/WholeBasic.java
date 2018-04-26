package cn.wholeplan;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Transaction;

import cn.pojo.Order;
import cn.pojo.Process;
import cn.pojo.Task;
import cn.pojo.Wop;
import cn.utils.Basic;

/*
 * 本类是给整体排产提供基础方法
 * 
 * 1.caculateWopEST(List<Wop>, boolean)用于推式排产，以一个关键工序的Wop集合为输入，从上一道关键工序推，计算该工序最早开始时间，如果没有
 * 上一道关键工序，以0时刻为最早开始时间，第二个参数为是否存储数据库
 * 2.pushByProcess(int, boolean)在1计算最早开始时间的基础上，对某关键工序的所有wop排产，以最早可以开始的任务放在最早可以加工的机台上为规则，
 * 计算每一个wop的开始时间和结束时间
 * 3caculateWopLET(List<Wop>, boolean)，4pullByProcess(int, boolean)为拉式的同等方法，只是以最晚结束时间为参考时间逆向拉式排产。
 * */

public class WholeBasic extends Basic {
	
	@SuppressWarnings("unchecked")
	static List<Process> processes = service.list(Process.class);
	static Process nonkeyProcess = (Process) service.get(Process.class,0);
	static int divideIdx = 0;
	static {
		//去掉非关键工序
		processes.remove(nonkeyProcess);
		//并得到分界线divideIdx,即电镀工序的编号
		for(int i=0;i<processes.size();i++) {
			if(processes.get(i).getProcessId()==10) {
				divideIdx = i;
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void pushByProcess(int nowProcessId,boolean updateDB) {
		Process nowProcess = (Process) service.get(Process.class, nowProcessId);
		List<Wop> wopforProcess = service.list(Wop.class,"process",nowProcess);
		
		//计算最早开始时间
		caculateWopEST(wopforProcess,updateDB);
		
		//为每个wopforProcess排程
		Collections.sort(wopforProcess, (o1,o2)->(o1.getEarlistStartTime().isBefore(o2.getEarlistStartTime())?-1:1));
		
		List<Task> keyTasks = new LinkedList<>();
		double[] machines = new double[nowProcess.getMainResource()];
		for(Wop wop:wopforProcess) {
			int machine = findMin(machines);
			double startTime = Math.max(zeroTime.until(wop.getEarlistStartTime(), ChronoUnit.MINUTES), machines[machine]);
			double endTime = startTime+ wop.getProcessTime();
			machines[machine] = endTime;
			
			Task task = new Task();
			task.setWop(wop);
			task.setOrder(wop.getOrder());
			task.setProcess(nowProcess);
			task.setStartTime(zeroTime.plusMinutes((long) startTime));
			task.setEndTime(zeroTime.plusMinutes((long) endTime));
			task.setResuorceId(machine+1);
			keyTasks.add(task);
		}
		
		if(updateDB) {
			Transaction tx = service.beginTransaction();
			service.save(keyTasks);
			tx.commit();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void pullByProcess(int nowProcessId,boolean updateDB) {
		Process nowProcess = (Process) service.get(Process.class, nowProcessId);
		List<Wop> wopforProcess = service.list(Wop.class,"process",nowProcess);
		
		//计算最晚结束时间
		caculateWopLET(wopforProcess,updateDB);
		
		//为每个wopforProcess排程
		Collections.sort(wopforProcess, (o1,o2)->(o1.getLatestEndTime().isBefore(o2.getLatestEndTime())?1:-1));
		
		List<Task> keyTasks = new LinkedList<>();
		double[] machines = new double[nowProcess.getMainResource()];
		for(int i=0;i<machines.length;i++) {
			machines[i] = 2880;
		}
		for(Wop wop:wopforProcess) {
			int machine = findMax(machines);
			double endTime = Math.min(zeroTime.until(wop.getLatestEndTime(), ChronoUnit.MINUTES), machines[machine]);
			double startTime = endTime - wop.getProcessTime();
			machines[machine] = startTime;
			
			Task task = new Task();
			task.setWop(wop);
			task.setOrder(wop.getOrder());
			task.setProcess(nowProcess);
			task.setStartTime(zeroTime.plusMinutes((long) startTime));
			task.setEndTime(zeroTime.plusMinutes((long) endTime));
			task.setResuorceId(machine+1);
			keyTasks.add(task);
		}
		
		if(updateDB) {
			Transaction tx = service.beginTransaction();
			service.save(keyTasks);
			tx.commit();
		}
	}
	
	//先为每个wop设置最早开始时间,并为中间的非关键工序设置Task,第二个参数代表是否将非关键工序的Task存储到数据库
	@SuppressWarnings("unchecked")
	public void caculateWopEST(List<Wop> wopforProcess,boolean updateDB) {
		
		List<Task> nonKeyTasks = new LinkedList<>();
		for (Wop wop : wopforProcess) {
			Order order = wop.getOrder();
			List<Wop> wopforOrder = service.list(Wop.class,"order",order);
			
			Collections.sort(wopforOrder, (o1,o2)->o1.getWopId().compareTo(o2.getWopId()));
			//找到上一道关键工序
			int lastIdx = -1,nowIdx = wopforOrder.size()-1;
			for (int i=0;i<wopforOrder.size();i++) {
				if(wopforOrder.get(i).equals(wop)) {
					nowIdx = i;
					break;
				}
				if(processes.contains(wopforOrder.get(i).getProcess())) {
					lastIdx = i;
				}
			}
			
			//没有上工序，那么最早开始时间为0
			if(lastIdx == -1) {
				wop.setEarlistStartTime(zeroTime);
			}
			else{
				List<Task> TaskList = service.list(Task.class,"wop",wopforOrder.get(lastIdx));
				//如果上关键工序还没排产，最早开始时间为0
				if(TaskList.isEmpty()) {
					wop.setEarlistStartTime(zeroTime);
				}
				//如果上关键工序已排产，那么便依次推到此工序
				else {
					Task lastTask = TaskList.get(0);
					LocalDateTime time = lastTask.getEndTime();
					int i=lastIdx+1;
					for(;i<nowIdx;i++) {
						Task nonkey  = new Task();
						nonkey.setWop(wopforOrder.get(i));
						nonkey.setOrder(order);
						nonkey.setProcess(nonkeyProcess);
						nonkey.setStartTime(time);
						time = time.plusMinutes((long) wopforOrder.get(i).getProcessTime());
						nonkey.setEndTime(time);
						nonkey.setResuorceId(1);
						nonKeyTasks.add(nonkey);
					}
					wopforOrder.get(i).setEarlistStartTime(time);
				}
			}
//					System.out.println(wop);
		}
		if(updateDB) {
			Transaction tx = service.beginTransaction();
			service.save(nonKeyTasks);
			tx.commit();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void caculateWopLET(List<Wop> wopforProcess,boolean updateDB){
		List<Task> nonKeyTasks = new LinkedList<>();
		for (Wop wop : wopforProcess) {
			Order order = wop.getOrder();
			List<Wop> wopforOrder = service.list(Wop.class,"order",order);
			
			Collections.sort(wopforOrder, (o1,o2)->o2.getWopId().compareTo(o1.getWopId()));
			//找到上一道关键工序
			int lastIdx = -1,nowIdx = wopforOrder.size()-1;
			for (int i=0;i<wopforOrder.size();i++) {
				if(wopforOrder.get(i).equals(wop)) {
					nowIdx = i;
					break;
				}
				if(processes.contains(wopforOrder.get(i).getProcess())) {
					lastIdx = i;
				}
			}
			
			//没有上工序，那么最晚结束时间为-120，两个小时Buffer
			if(lastIdx == -1) {
				wop.setLatestEndTime(zeroTime.minusMinutes(120));
			}
			else{
				List<Task> TaskList = service.list(Task.class,"wop",wopforOrder.get(lastIdx));
				//如果上关键工序还没排产，最早开始时间为-120
				if(TaskList.isEmpty()) {
					wop.setLatestEndTime(zeroTime.minusMinutes(120));
				}
				//如果上关键工序已排产，那么便依次推到此工序
				else {
					Task lastTask = TaskList.get(0);
					LocalDateTime time = lastTask.getStartTime();
					//如果上一道工序是电镀，顺延2小时Buffer
					if(lastTask.getProcess().getProcessId()==10) {
						time = time.minusMinutes(120);
					}
					int i=lastIdx+1;
					for(;i<nowIdx;i++) {
						Task nonkey  = new Task();
						nonkey.setWop(wopforOrder.get(i));
						nonkey.setOrder(order);
						nonkey.setProcess(nonkeyProcess);
						nonkey.setEndTime(time);
						time = time.minusMinutes((long) wopforOrder.get(i).getProcessTime());
						nonkey.setStartTime(time);
						nonkey.setResuorceId(1);
						nonKeyTasks.add(nonkey);
					}
					wopforOrder.get(i).setLatestEndTime(time);
				}
			}
//			System.out.println(wop);
		}
		if(updateDB) {
			Transaction tx = service.beginTransaction();
			service.save(nonKeyTasks);
			tx.commit();
		}
	}
}
