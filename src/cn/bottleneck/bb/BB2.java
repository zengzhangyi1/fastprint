package cn.bottleneck.bb;

import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;

import cn.utils.HibernateUtils;

/* Brunch and Bound����֧���編
 * �汾2�Ƿ������븺�����ų�װ�غ�ж��
 * */
public class BB2 {
	public static void main(String[] args) {
		BB2 bb = new BB2();
		/*
		 * ��һ�������ǽڵ�ﵽ��������һ��
		 * �ڶ���������ÿ����������Ϊ����
		 * */
		bb.algorithmRun(100000,500);
		HibernateUtils.closeAll();
	}
	
	public void algorithmRun(int clearNum,int holdNum) {
		BBNode2 rootNode = new BBNode2(true);
		double upperBound = rootNode.getUpperBound()+100;
		TreeSet<BBNode2> tree = new TreeSet<>();
		tree.add(rootNode);
		int clearTimes = 0, outCount=0;
		Date beginTime  = new Date();
		
		//�����Ĺ��ﵽһ��������������һ������������ǰһЩ����
		while(!tree.first().getUndo().isEmpty()) {
			BBNode2 firstNode = tree.pollFirst();
			if(tree.size()>clearNum) {
				LinkedList<BBNode2> holdNodeList = new LinkedList<>();
				for(int i = 0;i<holdNum;i++) {
					holdNodeList.add(tree.pollFirst());
				}
				tree.clear();
				tree.addAll(holdNodeList);
				clearTimes++;
			}
			outCount++;
			if(outCount==100) {
				System.out.println("treeSize:"+tree.size()+","+firstNode);
				outCount = 0;
			}
			tree.addAll(firstNode.generate(upperBound));
		}
		Date endTime = new Date();
		long runtime = (endTime.getTime() -beginTime.getTime());
		System.out.println("===========best============");
		System.out.println(tree.first());
		System.out.println("undo:"+tree.first().getUndo());
		System.out.println("clearTimes:"+clearTimes);
		System.out.println("runTime:"+runtime+"ms");
		System.out.println(runtime+"	"+tree.first().getMakespan());
	}
}
