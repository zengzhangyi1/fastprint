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
	ArrayList<Integer> done;		//基于0
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
	//得到上界
	public double getUpperBound() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//电镀池最早开始时间
		boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
		double hoist = 0;	//抓钩最早开始时间
		List<Job> undoJobList = new ArrayList<>();
		for(int i = 0;i<jobs.size();i++) {
			undoJobList.add(jobs.get(i));
		}
		//按照加工时间从小到大将Job排序
		Collections.sort(undoJobList,(o1,o2)->(int)(o1.getProcessTime()-o2.getProcessTime()));
		
		int busytank = 0;	//记录工作中的电镀池数

		boolean lastload = true;
		
		//循环直到没有工作可以装载，且所有工作均卸载
		while(!undoJobList.isEmpty()) {
			int tank = findMin(startTime);	//找到最早结束的电镀池
			/*如果被占用，说明是卸载*/
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
			/*否则为装载*/
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				lastload = true;
				//计算电镀池开始时间
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
		//没有job，只剩下卸载
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
	
	//估算BBNode的makespan值，最小加工时间的任务安排在最早开始的资源上
	public double caculateMakespan1() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//电镀池最早开始时间
		boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
		double hoist = 0;	//抓钩最早开始时间
		int busytank = 0;	//记录工作中的电镀池数
		boolean lastload = true;
		double time = 0;
		
		List<Integer> a = new ArrayList<>();
		
		//将所有加工确定的放在undoJobList前面
		for(int i=0;i<done.size();i++) {
			a.add(done.get(i));
		}
		
		//将所有尚未加工的job加入undoJobList
		Set<Double> keySet = undo.keySet();
		TreeSet<Double> processTimeTree = new TreeSet<>();
		//将所有的加工时间有序的放入TreeSet
		for (Double double1 : keySet) {
			if(double1>0) {
				processTimeTree.add(double1);
			}
		}
		//将加工时间从小到大有序的放入undoJobList
		while(!processTimeTree.isEmpty()) {
			for(Integer i:undo.get(processTimeTree.pollFirst())) {
				a.add(i);
			}
		}
		
		//循环直到没有工作可以装载，且所有工作均卸载
		for(int i = 0;i<a.size();) {
			int tank = findMin(startTime);	//找到最早结束的电镀池
			//如果被占用，说明是卸载
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
			//否则为装载
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				lastload = true;
				//计算电镀池开始时间
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

		//没有job，只剩下卸载
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
	
	//随机生成不确定解
	public double caculateMakespan2(int circleTimes) {
		double minMakespan = Double.MAX_VALUE;
		for(int time=0;time<circleTimes;time++) {
			int tankNum = ResourceUtils.getTankNum();
			double startTime[] = new double[tankNum];	//电镀池最早开始时间
			boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
			double hoist = 0;	//抓钩最早开始时间
			int busytank = 0;	//记录工作中的电镀池数
			boolean lastload = true;
			
			List<Integer> a = new ArrayList<>();
			List<Integer> temp = new ArrayList<>();
			//将所有加工确定的放在undoJobList前面
			for(int i=0;i<done.size();i++) {
				a.add(done.get(i));
			}
			
			//将所有尚未加工的job加入undoJobList
			Set<Double> keySet = undo.keySet();
			//将所有的加工时间有序的放入TreeSet
			for (Double double1 : keySet) {
				Iterator<Integer> iterator = undo.get(double1).iterator();
				while(iterator.hasNext()) {
					temp.add(iterator.next());
				}
			}
			
			//后面的随机排列
			Collections.shuffle(temp);
			a.addAll(temp);
			
			//循环直到没有工作可以装载，且所有工作均卸载
			for(int i = 0;i<a.size();) {
				int tank = findMin(startTime);	//找到最早结束的电镀池
				//如果被占用，说明是卸载
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
				//否则为装载
				else {
					if(lastload) {
						hoist += ResourceUtils.getCircleTime();
					}
					busytank++;
					lastload = true;
					//计算电镀池开始时间
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

			//没有job，只剩下卸载
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
	
	
	
	//估算BBNode的makespan值，选取平均时间为90的任务安排在最早开始的资源上
	/*public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//电镀池最早开始时间
		boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
		double hoist = 0;	//抓钩最早开始时间
		int busytank = 0;
		double [] processTime = new double[tankNum];
		boolean lastload = true;
		
		计算已确定的任务的时间
		for(int i = 0;i<done.size();) {
			int tank = findMin(startTime);	//找到最早结束的电镀池
			//如果被占用，说明是卸载
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
			//否则为装载
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				busytank++;
				lastload = true;
				processTime[tank] = jobs.get(done.get(i)).getProcessTime();
				occupy[tank] = true;
				//计算电镀池开始时间
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
		
		估算未确定任务的时间
		//将所有尚未加工的jobid加入undoMap
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
		
		//计算不确定的时间
		while(!undoMap.isEmpty()) {
			int tank = findMin(startTime);	//找到最早结束的电镀池
			//如果被占用，说明是卸载
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
			//否则为装载
			else {
				if(lastload) {
					hoist += ResourceUtils.getCircleTime();
				}
				
				busytank++;
				lastload = true;
				occupy[tank] = true;
				
				//选job，选取平均时间能达到90的job
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
				
				//计算电镀池开始时间
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
		
		//没有job，只剩下卸载
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
	
	//繁衍下一代
	public List<BBNode> generate(double upperBound){
		List<BBNode> nextGneration = new ArrayList<>();
		Set<Entry<Double, LinkedList<Integer>>> entrySet = undo.entrySet();
		//每种加工时间生成一个子代
		for (Entry<Double, LinkedList<Integer>> entry : entrySet) {
			
			BBNode newNode = new BBNode();

			//复制上一代的done 和 undo
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
			
			//更新新一代的done 和 undo
			int updateJobNum = undo.get(entry.getKey()).getFirst();
			newdone.add(updateJobNum);
			newundo.get(entry.getKey()).remove(new Integer(updateJobNum));
			//如果该类任务没有了，便将undo中此加工时间的key移除
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
		return "makespan="+getMakespan()+"，done=" + done;
	}
	
}
