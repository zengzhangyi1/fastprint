package cn.utils;

import java.time.LocalDateTime;

import cn.service.Service;

public class Basic {
	static protected Service service = Service.getService();
	static protected LocalDateTime zeroTime = LocalDateTime.of(2017, 1, 1, 8, 0);
	//寻找最小加工时间的下标
	protected int findMin(double[] startTime) {
		int min = 0;
		for(int i = 1;i<startTime.length;i++) {
			if(startTime[i]<startTime[min]) {
				min=i;
			}
		}
		return min;
	}
	
	//寻找最大加工时间的下标
	protected int findMax(double[] startTime) {
		int max = 0;
		for(int i = 1;i<startTime.length;i++) {
			if(startTime[i]>startTime[max]) {
				max=i;
			}
		}
		return max;
	}
}
