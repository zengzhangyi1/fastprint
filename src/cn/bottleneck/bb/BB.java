package cn.bottleneck.bb;

import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;

import cn.bottleneck.BottleneckBasic;
import cn.utils.HibernateUtils;
import cn.utils.IOUtil;
import cn.utils.ResourceUtils;
import gantt.GanttChart;

/* Brunch and Bound����֧���編
 * �汾1ֻ�ų�װ�أ�ж���ù������
 * */
public class BB {
	public static void main(String[] args) {
		BB bb = new BB();
		/*
		 * ��һ�������ǽڵ�ﵽ��������һ��
		 * �ڶ���������ÿ����������Ϊ����
		 * �����������Ƿ�֧ʱ��������Ľ���
		 * ���ĸ������Ƿ�չʾ����ͼ
		 * */
		bb.algorithmRun(50000,100,50,true);
		HibernateUtils.closeAll();
	}
	
	public void algorithmRun(int clearNum,int holdNum,int randomTime,boolean gantt) {
		BBNode rootNode = new BBNode(true);
		BBNode.setRandomTime(randomTime);
		double upperBound = rootNode.getUpperBound()+20;
		TreeSet<BBNode> tree = new TreeSet<>();
		tree.add(rootNode);
		int clearTimes = 0, outCount=0;
		Date beginTime  = new Date();
		
		//�����Ĺ��ﵽһ��������������һ������������ǰһЩ����
		while(!tree.first().getUndo().isEmpty()) {
			BBNode firstNode = tree.pollFirst();
			if(tree.size()>clearNum&&tree.first().getMakespan()<upperBound) {
				LinkedList<BBNode> holdNodeList = new LinkedList<>();
				for(int i = 0;i<holdNum;i++) {
					holdNodeList.add(tree.pollFirst());
				}
				tree.clear();
				tree.addAll(holdNodeList);
				clearTimes++;
			}
			outCount++;
			if(outCount==10) {
				System.out.println("treeSize:"+tree.size()+","+firstNode);
				outCount = 0;
			}
			tree.addAll(firstNode.generate(upperBound));
		}
		Date endTime = new Date();
		long runtime = (endTime.getTime() -beginTime.getTime());
		BBNode best = tree.first();
		best.encode(best.getDone(),false);
		
		if(gantt) {
			IOUtil.writeTxt(BottleneckBasic.jobs);
			GanttChart gant = new GanttChart(ResourceUtils.getTankNum(),(float)best.getMakespan(), best.getDone().size());
			gant.setVisible(true);
		}
		System.out.println("===========best============");
		System.out.println(best);
		System.out.println("AverageCompleteTime:"+best.getAverageCompleteTime());
		System.out.println("undo:"+best.getUndo());
		System.out.println("clearTimes:"+clearTimes);
		System.out.println("runTime:"+runtime+"ms");
		System.out.println(runtime+"	"+best.getMakespan());
	}
}
