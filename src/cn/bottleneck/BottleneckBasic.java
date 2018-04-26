package cn.bottleneck;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Transaction;

import cn.pojo.Job;
import cn.utils.Basic;
import cn.utils.IOUtil;
import cn.utils.ResourceUtils;
/*
 * 该类记载公用的jobs和一些私有的方法
 */
public class BottleneckBasic extends Basic{
	public static List<Job> jobs = new ArrayList<>();
	
	static {
		@SuppressWarnings("unchecked")
		ArrayList<Job> fulljobs = (ArrayList<Job>) service.list(Job.class);
		for(int i = 0;i<fulljobs.size();i++) {
			jobs.add(fulljobs.get(i));
		}
		
//		IOUtil.readExcel(jobs);
	}
	
	//根据编码解出makespan,第二个参数代表是否更新到数据库
	//基于0
	public double encode(List<Integer> a,boolean updateDB) {
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
				
				Job loadJob = jobs.get(a.get(i++));
				loadJob.setStartTime(hoist);
				loadJob.setEndTime(hoist+loadJob.getProcessTime());
				loadJob.setResourceId(tank+1);
				
				
				busytank++;
				lastload = true;
				//计算电镀池开始时间
				for(int j =0;j<startTime.length;j++) {
					if(j==tank) {
						startTime[j] = hoist+ loadJob.getProcessTime()+ResourceUtils.getCircleTime();
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
		
		if(updateDB) {
			Transaction tx  = service.beginTransaction();
			for(int i=0;i<jobs.size();i++) {
				service.update(jobs.get(i));
			}
			tx.commit();
		}
		return hoist;
	}
	
	
	
	
	
	//寻找最小加工时间并且在工作的下标
	protected int findBusyMin(double[] startTime,boolean []occupy) {
		int min = -1;
		double minValue = Double.MAX_VALUE;
		for(int i = 0;i<startTime.length;i++) {
			if(startTime[i]<minValue&&occupy[i]) {
				min=i;
				minValue = startTime[i];
			}
		}
		return min;
	}
	
	protected int findUnloadTank(int[] job,int activity) {
		for(int i = 0;i<job.length;i++) {
			if(job[i]==Math.abs(activity)) {
				return i;
			}
		}
		return -1;
	}
	
	protected double sum(double []processTime,double num) {
		double sum = 0;
		for (double d : processTime) {
			sum += d;
		}
		sum += num;
		return sum;
	}
}
