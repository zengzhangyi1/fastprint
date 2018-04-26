package cn.bottleneck.bb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
	ArrayList<Integer> done;		//����0
	Map<Double,LinkedList<Integer>> undo;
	private double makespan;
	private double averageCompleteTime;
	private int busyTankNum;
	private static int randomTime;
	
	public BBNode() {
		
	}
	public BBNode(boolean first) {
		if(first) {
			done = new ArrayList<Integer>();
			undo = new HashMap<Double,LinkedList<Integer>>();
			for(int i = 0;i<jobs.size();i++) {
				if(undo.containsKey(jobs.get(i).getProcessTime())) {
					undo.get(jobs.get(i).getProcessTime()).add(i);
				}
				else {
					LinkedList<Integer> joblist = new LinkedList<>();
					joblist.add(i);
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
		Collections.sort(undoJobList,(o1,o2)->(int)(o1.getProcessTime()-o2.getProcessTime()));
		
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
					}
					else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}
						else {
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
				lastload = true;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ undoJobList.remove(0).getProcessTime()+ResourceUtils.getCircleTime();
						occupy[j] = true;
					}
					else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}
						else {
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
	
	public double caculateMakespan() {
		double min  = Math.min(caculateMakespan1(), caculateMakespan2(randomTime));
		return min;
	}
	
	//����BBNode��makespanֵ����С�ӹ�ʱ��������������翪ʼ����Դ��
	public double caculateMakespan1() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
		boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
		double hoist = 0;	//ץ�����翪ʼʱ��
		int busytank = 0;	//��¼�����еĵ�Ƴ���
		boolean lastload = true;
		double time = 0;
		
		List<Integer> a = new ArrayList<>();
		
		//�����мӹ�ȷ���ķ���undoJobListǰ��
		for(int i=0;i<done.size();i++) {
			a.add(done.get(i));
		}
		
		//��������δ�ӹ���job����undoJobList
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
				a.add(i);
			}
		}
		
		//ѭ��ֱ��û�й�������װ�أ������й�����ж��
		for(int i = 0;i<a.size();) {
			int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
			//�����ռ�ã�˵����ж��
			if(occupy[tank]) {
				hoist = startTime[tank];
				occupy[tank] = false;
				busytank--;
				lastload = false;
				time += hoist;
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
			//����Ϊװ��
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				lastload = true;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ jobs.get(a.get(i++)).getProcessTime()+ResourceUtils.getCircleTime();
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
			time += hoist;
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
		setAverageCompleteTime(time);
		setMakespan(hoist);
		return hoist;
	}
	
	//������ɲ�ȷ����
	public double caculateMakespan2(int circleTimes) {
		double minMakespan = Double.MAX_VALUE;
		for(int time=0;time<circleTimes;time++) {
			int tankNum = ResourceUtils.getTankNum();
			double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
			boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
			double hoist = 0;	//ץ�����翪ʼʱ��
			int busytank = 0;	//��¼�����еĵ�Ƴ���
			boolean lastload = true;
			
			List<Integer> a = new ArrayList<>();
			List<Integer> temp = new ArrayList<>();
			//�����мӹ�ȷ���ķ���undoJobListǰ��
			for(int i=0;i<done.size();i++) {
				a.add(done.get(i));
			}
			
			//��������δ�ӹ���job����undoJobList
			Set<Double> keySet = undo.keySet();
			//�����еļӹ�ʱ������ķ���TreeSet
			for (Double double1 : keySet) {
				Iterator<Integer> iterator = undo.get(double1).iterator();
				while(iterator.hasNext()) {
					temp.add(iterator.next());
				}
			}
			
			//������������
			Collections.shuffle(temp);
			a.addAll(temp);
			
			//ѭ��ֱ��û�й�������װ�أ������й�����ж��
			for(int i = 0;i<a.size();) {
				int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
				//�����ռ�ã�˵����ж��
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
				//����Ϊװ��
				else {
					if(lastload) {
						hoist += ResourceUtils.getCircleTime();
					}
					busytank++;
					lastload = true;
					//�����Ƴؿ�ʼʱ��
					for(int j =0;j<startTime.length;j++) {
						if(j==tank) {
							startTime[j] = hoist+ jobs.get(a.get(i++)).getProcessTime()+ResourceUtils.getCircleTime();
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
			minMakespan = Math.min(hoist, minMakespan);
		}
		
		setMakespan(minMakespan);
		return minMakespan;
	}
	
	
	
	//����BBNode��makespanֵ��ѡȡƽ��ʱ��Ϊ90�������������翪ʼ����Դ��
	/*public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
		boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
		double hoist = 0;	//ץ�����翪ʼʱ��
		int busytank = 0;
		double [] processTime = new double[tankNum];
		boolean lastload = true;
		
		������ȷ���������ʱ��
		for(int i = 0;i<done.size();) {
			int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
			//�����ռ�ã�˵����ж��
			if(occupy[tank]) {
				hoist = startTime[tank];
				occupy[tank] = false;
				busytank--;
				lastload = false;
				processTime[tank] = 0;
				
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
			//����Ϊװ��
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				lastload = true;
				processTime[tank] = jobs.get(done.get(i)).getProcessTime();
				occupy[tank] = true;
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ jobs.get(done.get(i++)).getProcessTime()+ResourceUtils.getCircleTime();
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
		
		����δȷ�������ʱ��
		//��������δ�ӹ���jobid����undoMap
		Map<Double,LinkedList<Integer>> undoMap = new HashMap<>();
		Set<Entry<Double, LinkedList<Integer>>> entrySet = undo.entrySet();
		for (Entry<Double, LinkedList<Integer>> entry : entrySet) {
			double key = entry.getKey();
			LinkedList<Integer> value = entry.getValue();
			LinkedList<Integer> undoList = new LinkedList<>();
			for (Integer integer : value) {
				undoList.add(integer);
			}
			undoMap.put(key, undoList);
		}
		
		//���㲻ȷ����ʱ��
		while(!undoMap.isEmpty()) {
			int tank = findMin(startTime);	//�ҵ���������ĵ�Ƴ�
			//�����ռ�ã�˵����ж��
			if(occupy[tank]) {
				hoist = startTime[tank];
				occupy[tank] = false;
				busytank--;
				lastload = false;
				processTime[tank] = 0;
				
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
			//����Ϊװ��
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				
				busytank++;
				lastload = true;
				occupy[tank] = true;
				
				//ѡjob��ѡȡƽ��ʱ���ܴﵽ90��job
				double averageTime = Double.MAX_VALUE,pickProcessTime = 0;
				Set<Double> keySet = undoMap.keySet();
				for (Double num : keySet) {
					double tempAver = Math.abs(sum(processTime,num)/busytank-90);
					if(tempAver<averageTime) {
						averageTime = tempAver;
						pickProcessTime = num;
					}
				}
				Job job = jobs.get(undoMap.get(pickProcessTime).removeFirst());
				if(undoMap.get(pickProcessTime).isEmpty()) undoMap.remove(pickProcessTime);
				
				processTime[tank] = job.getProcessTime();
				
				//�����Ƴؿ�ʼʱ��
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ job.getProcessTime()+ResourceUtils.getCircleTime();
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
	*/
	
	//������һ��
	public List<BBNode> generate(double upperBound){
		List<BBNode> nextGneration = new ArrayList<>();
		Set<Entry<Double, LinkedList<Integer>>> entrySet = undo.entrySet();
		//ÿ�ּӹ�ʱ������һ���Ӵ�
		for (Entry<Double, LinkedList<Integer>> entry : entrySet) {
			
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
			
			newNode.setDone(newdone);
			newNode.setUndo(newundo);
			newNode.caculateMakespan();
			if(newNode.getMakespan()<=upperBound) {
				nextGneration.add(newNode);
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
	public double getAverageCompleteTime() {
		return averageCompleteTime;
	}
	public void setAverageCompleteTime(double averageCompleteTime) {
		this.averageCompleteTime = averageCompleteTime;
	}
	public static int getRandomTime() {
		return randomTime;
	}
	public static void setRandomTime(int randomTime) {
		BBNode.randomTime = randomTime;
	}
	@Override
	public int compareTo(BBNode other) {
		return this.getMakespan()>other.getMakespan()?1:-1;
	}
	
	@Override
	public String toString() {
		return "makespan="+getMakespan()+"��done=" + done;
	}
	
}
