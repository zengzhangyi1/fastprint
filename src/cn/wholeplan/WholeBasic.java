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
 * �����Ǹ������Ų��ṩ��������
 * 
 * 1.caculateWopEST(List<Wop>, boolean)������ʽ�Ų�����һ���ؼ������Wop����Ϊ���룬����һ���ؼ������ƣ�����ù������翪ʼʱ�䣬���û��
 * ��һ���ؼ�������0ʱ��Ϊ���翪ʼʱ�䣬�ڶ�������Ϊ�Ƿ�洢���ݿ�
 * 2.pushByProcess(int, boolean)��1�������翪ʼʱ��Ļ����ϣ���ĳ�ؼ����������wop�Ų�����������Կ�ʼ���������������Լӹ��Ļ�̨��Ϊ����
 * ����ÿһ��wop�Ŀ�ʼʱ��ͽ���ʱ��
 * 3caculateWopLET(List<Wop>, boolean)��4pullByProcess(int, boolean)Ϊ��ʽ��ͬ�ȷ�����ֻ�����������ʱ��Ϊ�ο�ʱ��������ʽ�Ų���
 * */

public class WholeBasic extends Basic {
	
	@SuppressWarnings("unchecked")
	static List<Process> processes = service.list(Process.class);
	static Process nonkeyProcess = (Process) service.get(Process.class,0);
	static int divideIdx = 0;
	static {
		//ȥ���ǹؼ�����
		processes.remove(nonkeyProcess);
		//���õ��ֽ���divideIdx,����ƹ���ı��
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
		
		//�������翪ʼʱ��
		caculateWopEST(wopforProcess,updateDB);
		
		//Ϊÿ��wopforProcess�ų�
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
		
		//�����������ʱ��
		caculateWopLET(wopforProcess,updateDB);
		
		//Ϊÿ��wopforProcess�ų�
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
	
	//��Ϊÿ��wop�������翪ʼʱ��,��Ϊ�м�ķǹؼ���������Task,�ڶ������������Ƿ񽫷ǹؼ������Task�洢�����ݿ�
	@SuppressWarnings("unchecked")
	public void caculateWopEST(List<Wop> wopforProcess,boolean updateDB) {
		
		List<Task> nonKeyTasks = new LinkedList<>();
		for (Wop wop : wopforProcess) {
			Order order = wop.getOrder();
			List<Wop> wopforOrder = service.list(Wop.class,"order",order);
			
			Collections.sort(wopforOrder, (o1,o2)->o1.getWopId().compareTo(o2.getWopId()));
			//�ҵ���һ���ؼ�����
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
			
			//û���Ϲ�����ô���翪ʼʱ��Ϊ0
			if(lastIdx == -1) {
				wop.setEarlistStartTime(zeroTime);
			}
			else{
				List<Task> TaskList = service.list(Task.class,"wop",wopforOrder.get(lastIdx));
				//����Ϲؼ�����û�Ų������翪ʼʱ��Ϊ0
				if(TaskList.isEmpty()) {
					wop.setEarlistStartTime(zeroTime);
				}
				//����Ϲؼ��������Ų�����ô�������Ƶ��˹���
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
			//�ҵ���һ���ؼ�����
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
			
			//û���Ϲ�����ô�������ʱ��Ϊ-120������СʱBuffer
			if(lastIdx == -1) {
				wop.setLatestEndTime(zeroTime.minusMinutes(120));
			}
			else{
				List<Task> TaskList = service.list(Task.class,"wop",wopforOrder.get(lastIdx));
				//����Ϲؼ�����û�Ų������翪ʼʱ��Ϊ-120
				if(TaskList.isEmpty()) {
					wop.setLatestEndTime(zeroTime.minusMinutes(120));
				}
				//����Ϲؼ��������Ų�����ô�������Ƶ��˹���
				else {
					Task lastTask = TaskList.get(0);
					LocalDateTime time = lastTask.getStartTime();
					//�����һ�������ǵ�ƣ�˳��2СʱBuffer
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
