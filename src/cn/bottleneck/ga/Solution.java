package cn.bottleneck.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bottleneck.BottleneckBasic;
import cn.pojo.Job;
import cn.utils.ResourceUtils;

public class Solution extends BottleneckBasic{
	private List<Integer> a = new ArrayList<>();
	private double makespan;
	
	//生成随机解
	public void RandomSolution() {
		int solutionLength = jobs.size();
		for(int i = 0;i<solutionLength;i++) {
			a.add(i);
		}
		Collections.shuffle(a);
	}
	
	//计算makespan
	public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//电镀池最早开始时间
		boolean occupy[] = new boolean[tankNum];		//电镀池是否被占用
		double hoist = 0;	//抓钩最早开始时间
		int busytank = 0;	//记录工作中的电镀池数

		boolean lastload = true;
		
		//循环直到没有工作可以装载，且所有工作均卸载
		for(int i = 0;i<a.size();) {
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
		setMakespan(hoist);
		return hoist;
	}
	
	public double getfitness() {
		return 1/getMakespan();
	}
	
	
	public List<Integer> getA() {
		return a;
	}
	public void setA(List<Integer> a) {
		this.a = a;
	}
	public double getMakespan() {
		return makespan;
	}
	public void setMakespan(double makespan) {
		this.makespan = makespan;
	}
	public static List<Job> getJobs() {
		return jobs;
	}
	public static void setJobs(List<Job> jobs) {
		Solution.jobs = jobs;
	}
	
}
