package cn.utils;

import cn.service.Service;

public class ResourceUtils {
	final static double barLength = 360d;
	final static int tankNum;
	final static double circleTime = 6;
	
	static {
		Service service = Service.getService();
		tankNum = service.getProcessByName("µç¶Æ").getMainResource();
	}
	
	public static double getBarLength() {
		return barLength;
	}
	public static int getTankNum() {
		return tankNum;
	}
	
	public static double getCircleTime() {
		return circleTime;
	}
}
