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
	
	//���������
	public void RandomSolution() {
		int solutionLength = jobs.size();
		for(int i = 0;i<solutionLength;i++) {
			a.add(i);
		}
		Collections.shuffle(a);
	}
	
	//����makespan
	public double caculateMakespan() {
		int tankNum = ResourceUtils.getTankNum();
		double startTime[] = new double[tankNum];	//��Ƴ����翪ʼʱ��
		boolean occupy[] = new boolean[tankNum];		//��Ƴ��Ƿ�ռ��
		double hoist = 0;	//ץ�����翪ʼʱ��
		int busytank = 0;	//��¼�����еĵ�Ƴ���

		boolean lastload = true;
		
		//ѭ��ֱ��û�й�������װ�أ������й�����ж��
		for(int i = 0;i<a.size();) {
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
