package cn.bottleneck;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Transaction;

import cn.pojo.Job;
import cn.utils.Basic;
import cn.utils.IOUtil;
import cn.utils.ResourceUtils;
/*
 * ������ع��õ�jobs��һЩ˽�еķ���
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
	
	//���ݱ�����makespan,�ڶ������������Ƿ���µ����ݿ�
	//����0
	public double encode(List<Integer> a,boolean updateDB) {
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
				
				Job loadJob = jobs.get(a.get(i++));
				loadJob.setStartTime(hoist);
				loadJob.setEndTime(hoist+loadJob.getProcessTime());
				loadJob.setResourceId(tank+1);
				
				
				busytank++;
				lastload = true;
				//�����Ƴؿ�ʼʱ��
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
		
		if(updateDB) {
			Transaction tx  = service.beginTransaction();
			for(int i=0;i<jobs.size();i++) {
				service.update(jobs.get(i));
			}
			tx.commit();
		}
		return hoist;
	}
	
	
	
	
	
	//Ѱ����С�ӹ�ʱ�䲢���ڹ������±�
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
