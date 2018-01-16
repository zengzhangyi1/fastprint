package cn.bottleneck;

import java.util.ArrayList;
import java.util.List;

import cn.pojo.Job;
import cn.service.Service;
/*
 * ������ع��õ�jobs��һЩ˽�еķ���
 */
public class BottleneckBasic {
	protected static List<Job> jobs = new ArrayList<>();
	static {
		Service service = new Service();
		@SuppressWarnings("unchecked")
		ArrayList<Job> fulljobs = (ArrayList<Job>) service.list(Job.class);
		for(int i = 0;i<100;i++) {
			jobs.add(fulljobs.get(i));
		}
	}
	
	//Ѱ����С�ӹ�ʱ����±�
	protected int findMin(double[] startTime) {
		int min = 0;
		for(int i = 1;i<startTime.length;i++) {
			if(startTime[i]<startTime[min]) {
				min=i;
			}
		}
		return min;
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
			if(job[i]==(-activity)) {
				return i;
			}
		}
		return -1;
	}
}
