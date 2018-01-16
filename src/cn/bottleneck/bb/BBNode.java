package cn.bottleneck.bb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import cn.bottleneck.BottleneckBasic;
import cn.pojo.Job;
import cn.utils.ResourceUtils;

public class BBNode extends BottleneckBasic implements Comparable<BBNode> {
	ArrayList<Integer> done;
	Map<Double,LinkedList<Integer>> undo;
	private double makespan;
	private int busyTankNum;
	
	public BBNode() {
		
	}
	public BBNode(boolean first) {
		if(first) {
			done = new ArrayList<Integer>();
			undo = new HashMap<Double,LinkedList<Integer>>();
			for(int i = 0;i<jobs.size();i++) {
				if(undo.containsKey(jobs.get(i).getProcessTime())) {
					undo.get(jobs.get(i).getProcessTime()).add(i+1);
				}
				else {
					LinkedList<Integer> joblist = new LinkedList<>();
					joblist.add(i+1);
					undo.put(jobs.get(i).getProcessTime(), joblist);
				}
			}
			caculateMakespan();
		}
	}
	//�õ��Ͻ�
	public double getUpperBound() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
		boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
		double hoist = 0;	//ץ�����翪ʼʱ��
		List<Job> undoJobList = new ArrayList<>();
		for(int i = 0;i<jobs.size();i++) {
			undoJobList.add(jobs.get(i));
		}
		//���ռӹ�ʱ���С����Job����
		Collections.sort(undoJobList,new Comparator<Job>() {

			@Override
			public int compare(Job o1, Job o2) {
				return (int)(o1.getProcessTime()-o2.getProcessTime());
			}
			
		});
		
		int busytank = 0;	//��¼�����еĵ�Ƴ���

		boolean lastload = true;
		
		//ѭ��ֱ��û�й�������װ�أ������й�����ж��
		while(!undoJobList.isEmpty()) {
			int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
			/*�����ռ�ã�˵����ж��*/
			if(occupy[tank]) {
				hoist = startTime[tank];
				occupy[tank] = false;
				busytank--;
				lastload = false;
				
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
			/*����Ϊװ��*/
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ undoJobList.remove(0).getProcessTime()+ResourceUtils.getCircleTime();
						occupy[j] = true;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
		}
		//û��job��ֻʣ��ж��
		while(busytank>0) {
			int tank = findBusyMin(startTime, occupy);
			hoist = startTime[tank];
			occupy[tank] = false;
			busytank--;
			
			for(int j =0;j<startTime.length;j++) {
				if(j==tank) {
					startTime[j] = hoist;
				}else {
					if(occupy[j]) {
						startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
					}else {
						startTime[j]=hoist;
					}
				}
			}
		}
		
		return hoist;
	}
	//�õ�����ɹ����ļӹ�ʱ��
	public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
		boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
		int job[] = new int[tankNum];				//ÿ����Ƴش�ŵĹ�����
		double hoist = 0;	//ץ�����翪ʼʱ��
		
		/*������ȷ���������ʱ��*/
		for(int i=0;i<done.size();i++) {
			int activity = done.get(i);
			
			/*װ��*/
			if(activity>0) {
				//����ץ����ʼʱ��
				//ǰһ��Ҳ��װ�أ�����ҪcircleTime
				if(i==0||done.get(i-1)>0) {
					hoist += ResourceUtils.getCircleTime();
				}
				
				int tank = findMin(startTime);	//�ж�װ�صĵ�Ƴغ�
				job[tank] = activity;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ jobs.get(activity-1).getProcessTime()+ResourceUtils.getCircleTime();
						occupy[j] = true;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
			/*ж��*/
			else {
				//�ҵ�ж�صĻ�̨��
				int tank = findUnloadTank(job, activity);
				hoist = startTime[tank];
				occupy[tank] = false;
				job[tank] = 0;
				
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
		}
		
		/*����δȷ�������ʱ��*/
		//��������δ�ӹ���job����undoJobList
		List<Job> undoJobList = new ArrayList<>();
		Set<Double> keySet = undo.keySet();
		TreeSet<Double> processTimeTree = new TreeSet<>();
		//�����еļӹ�ʱ������ķ���TreeSet
		for (Double double1 : keySet) {
			if(double1>0) {
				processTimeTree.add(double1);
			}
		}
		//���ӹ�ʱ���С��������ķ���undoJobList
		while(!processTimeTree.isEmpty()) {
			for(Integer i:undo.get(processTimeTree.pollFirst())) {
				undoJobList.add(jobs.get(i-1));
			}
		}
		
		int busytank = 0;	//��¼�����еĵ�Ƴ���
		for(boolean b:occupy) {
			if(b) {
				busytank++;
			}
		}
		setBusyTankNum(busytank);
		boolean lastload = false;
		if(done.size()==0||done.get(done.size()-1)>0) {
			lastload = true;
		}
		
		//ѭ��ֱ��û�й�������װ�أ������й�����ж��
		while(!undoJobList.isEmpty()) {
			int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
			/*�����ռ�ã�˵����ж��*/
			if(occupy[tank]) {
				hoist = startTime[tank];
				occupy[tank] = false;
				busytank--;
				lastload = false;
				
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
			/*����Ϊװ��*/
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ undoJobList.remove(0).getProcessTime()+ResourceUtils.getCircleTime();
						occupy[j] = true;
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
							startTime[j]=hoist;
						}
					}
				}
			}
		}
		//û��job��ֻʣ��ж��
		while(busytank>0) {
			int tank = findBusyMin(startTime, occupy);
			hoist = startTime[tank];
			occupy[tank] = false;
			busytank--;
			
			for(int j =0;j<startTime.length;j++) {
				if(j==tank) {
					startTime[j] = hoist;
				}else {
					if(occupy[j]) {
						startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
					}else {
						startTime[j]=hoist;
					}
				}
			}
		}
		setMakespan(hoist);
		return hoist;
	}
	
	//������һ��
	public List<BBNode> generate(double upperBound){
		List<BBNode> nextGneration = new ArrayList<>();
		Set<Entry<Double, LinkedList<Integer>>> entrySet = undo.entrySet();
		double unloadkey = (double)-1;
		//�п��е�Ƴز�������װ��
		if(getBusyTankNum()<ResourceUtils.getTankNum()) {
			for (Entry<Double, LinkedList<Integer>> entry : entrySet) {
				
				//��������ӹ�ʱ�����0����ÿ�ּӹ�ʱ��ֻ����һ���Ӵ�
				if(entry.getKey()>0) {
					
					BBNode newNode = new BBNode();

					//������һ����done �� undo
					ArrayList<Integer> newdone = new ArrayList<>();
					Map<Double,LinkedList<Integer>> newundo = new HashMap<>();
					for(Integer i:done) {
						newdone.add(i);
					}
					Set<Double> keySet = undo.keySet();
					for (Double double1 : keySet) {
						LinkedList<Integer> processTimeList = new LinkedList<>();
						for(Integer i:undo.get(double1)) {
							processTimeList.add(i);
						}
						newundo.put(double1, processTimeList);
					}
					
					//������һ����done �� undo
					int updateJobNum = undo.get(entry.getKey()).getFirst();
					newdone.add(updateJobNum);
					newundo.get(entry.getKey()).remove(new Integer(updateJobNum));
					//�����������û���ˣ��㽫undo�д˼ӹ�ʱ���key�Ƴ�
					if(newundo.get(entry.getKey()).isEmpty()) {
						newundo.remove(entry.getKey());
					}
					//װ��һ�����񣬱������һ��������Ĵ�ж��
					if(newundo.containsKey(unloadkey)) {
						newundo.get(unloadkey).add(-updateJobNum);
					}
					else {
						LinkedList<Integer> unloadJobList = new LinkedList<>();
						unloadJobList.add(-updateJobNum);
						newundo.put(unloadkey, unloadJobList);
					}
					newNode.setDone(newdone);
					newNode.setUndo(newundo);
					newNode.caculateMakespan();
					if(newNode.getMakespan()<=upperBound) {
						nextGneration.add(newNode);
					}
				}
			}
		}
		
		//���۵�Ƴ���״����Σ�ֻҪ��ж�ص�������Ҫ�����Ӵ�
		
		if(undo.containsKey(unloadkey)) {
			for(Integer index:undo.get(unloadkey)) {
				BBNode newNode = new BBNode();

				//������һ����done �� undo
				ArrayList<Integer> newdone = new ArrayList<>();
				Map<Double,LinkedList<Integer>> newundo = new HashMap<>();
				for(Integer i:done) {
					newdone.add(i);
				}
				Set<Double> keySet = undo.keySet();
				for (Double double1 : keySet) {
					LinkedList<Integer> processTimeList = new LinkedList<>();
					for(Integer i:undo.get(double1)) {
						processTimeList.add(i);
					}
					newundo.put(double1, processTimeList);
				}
				
				//������һ����done �� undo
				int updateJobNum = index;
				newdone.add(updateJobNum);
				newundo.get(unloadkey).remove(new Integer(updateJobNum));
				if(newundo.get(unloadkey).isEmpty()) {
					newundo.remove(unloadkey);
				}
				
				newNode.setDone(newdone);
				newNode.setUndo(newundo);
				newNode.caculateMakespan();
				if(newNode.getMakespan()<=upperBound) {
					nextGneration.add(newNode);
				}
			}
		}
		
		return nextGneration;
	}
	
	public static List<Job> getJobs() {
		return jobs;
	}
	public static void setJobs(List<Job> jobs) {
		BBNode.jobs = jobs;
	}
	public ArrayList<Integer> getDone() {
		return done;
	}
	public void setDone(ArrayList<Integer> done) {
		this.done = done;
	}
	
	public Map<Double, LinkedList<Integer>> getUndo() {
		return undo;
	}
	public void setUndo(Map<Double, LinkedList<Integer>> undo) {
		this.undo = undo;
	}
	public double getMakespan() {
		return makespan;
	}
	public void setMakespan(double makespan) {
		this.makespan = makespan;
	}
	public int getBusyTankNum() {
		return busyTankNum;
	}
	public void setBusyTankNum(int busyTankNum) {
		this.busyTankNum = busyTankNum;
	}
	@Override
	public int compareTo(BBNode other) {
		return this.getMakespan()<other.getMakespan()?-1:1;
	}
	
	@Override
	public String toString() {
		return "makespan="+getMakespan()+"��done=" + done;
	}
	
}
