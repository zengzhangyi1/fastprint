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
		Collections.sort(undoJobList,new Comparator<Job>() {

			@Override
			public int compare(Job o1, Job o2) {
				return (int)(o1.getProcessTime()-o2.getProcessTime());
			}
			
		});
		
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
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
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
				//计算电镀池开始时间
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
	//得到已完成工作的加工时间
	public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//电镀池最早开始时间
		boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
		int job[] = new int[tankNum];				//每个电镀池存放的工作号
		double hoist = 0;	//抓钩最早开始时间
		
		/*计算已确定的任务的时间*/
		for(int i=0;i<done.size();i++) {
			int activity = done.get(i);
			
			/*装载*/
			if(activity>0) {
				//计算抓钩开始时间
				//前一次也是装载，才需要circleTime
				if(i==0||done.get(i-1)>0) {
					hoist += ResourceUtils.getCircleTime();
				}
				
				int tank = findMin(startTime);	//判断装载的电镀池号
				job[tank] = activity;
				//计算电镀池开始时间
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
			/*卸载*/
			else {
				//找到卸载的机台号
				int tank = findUnloadTank(job, activity);
				hoist = startTime[tank];
				occupy[tank] = false;
				job[tank] = 0;
				
				//计算电镀池开始时间
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
		
		/*估算未确定任务的时间*/
		//将所有尚未加工的job加入undoJobList
		List<Job> undoJobList = new ArrayList<>();
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
				undoJobList.add(jobs.get(i-1));
			}
		}
		
		int busytank = 0;	//记录工作中的电镀池数
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
					}else {
						if(occupy[j]) {
							startTime[j]=Math.max(startTime[j], hoist+ResourceUtils.getCircleTime());
						}else {
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
				//计算电镀池开始时间
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
	
	//繁衍下一代
	public List<BBNode> generate(double upperBound){
		List<BBNode> nextGneration = new ArrayList<>();
		Set<Entry<Double, LinkedList<Integer>>> entrySet = undo.entrySet();
		double unloadkey = (double)-1;
		//有空闲电镀池才能生成装载
		if(getBusyTankNum()<ResourceUtils.getTankNum()) {
			for (Entry<Double, LinkedList<Integer>> entry : entrySet) {
				
				//工作任务加工时间大于0，则每种加工时间只生成一个子代
				if(entry.getKey()>0) {
					
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
					//装载一个任务，便会生成一个该任务的待卸载
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
		
		//无论电镀池数状况如何，只要有卸载的任务，则要生成子代
		
		if(undo.containsKey(unloadkey)) {
			for(Integer index:undo.get(unloadkey)) {
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
		return "makespan="+getMakespan()+"，done=" + done;
	}
	
}
