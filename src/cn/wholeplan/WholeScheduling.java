package cn.wholeplan;

import cn.utils.HibernateUtils;

public class WholeScheduling extends WholeBasic {
	public static void main(String[] args) {
		WholeScheduling ws = new WholeScheduling();
		ws.pushPull();
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	public void pushPull() {
		//推
		for(int processIdx = divideIdx+1;processIdx<processes.size();processIdx++) {
			pushByProcess(processes.get(processIdx).getProcessId(), true);
		}
		//拉
		for(int processIdx = divideIdx-1;processIdx>=0;processIdx--) {
			pullByProcess(processes.get(processIdx).getProcessId(), true);
		}
	}
	
}
