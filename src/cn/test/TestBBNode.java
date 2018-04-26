package cn.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import cn.bottleneck.bb.BBNode2;
import cn.bottleneck.bb.BB2;
import cn.bottleneck.bb.BBNode;
import cn.utils.HibernateUtils;

public class TestBBNode {
	@Test
	public void TestConstruction() {
		BBNode2 firstNode = new BBNode2(true);
		Set<Double> keySet = firstNode.getUndo().keySet();
		for (Double double1 : keySet) {
			System.out.println(double1+":"+firstNode.getUndo().get(double1));
		}
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestcalculateMakespan() {
		BBNode node = new BBNode(false);
		Integer [] a = {90, 30, 55, 0, 1, 31, 2, 3, 91, 4, 75, 5, 32, 6, 92, 76, 77, 7, 8, 9, 78, 10, 79, 11, 12, 13, 14, 80, 
				81, 15, 16, 17, 82, 83, 84, 93, 18, 19, 20, 56, 21, 22, 23, 57, 24, 25, 33, 58, 34, 35, 36, 26, 27, 28, 37, 38, 39
				, 40, 29, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 59, 60, 61, 62, 63, 64, 52,
				65, 66, 67, 53, 68, 69, 70, 54, 94, 95, 96, 97, 98, 99, 71, 85, 86, 87, 88, 89, 72, 73, 74};
		ArrayList<Integer> done = new ArrayList<>();
		for (Integer integer : a) {
			done.add(integer);
		}
		node.setUndo(new HashMap<>());
		node.setDone(done);
		System.out.println(node.caculateMakespan());
		System.out.println(node.encode(Arrays.asList(a),false));
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestgetUB() {
		BBNode2 node = new BBNode2(true);
		System.out.println(node.getUpperBound());
		/*ArrayList<Integer> undo = node.getUndo();
		for (Object object : undo) {
			System.out.println(object);
		}*/
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestGenerate() {
		BBNode2 node = new BBNode2(true);
		double upperBound = node.getUpperBound();
		List<BBNode2> gen2 = node.generate(upperBound);
		List<BBNode2> gen3 = gen2.get(0).generate(upperBound);
//		List<BBNode> gen4 = gen3.get(0).generate(upperBound);
//		double makespan4 = gen4.get(0).getMakespan(upperBound);
//		System.out.println(makespan4);
		for (BBNode2 bbNode : gen3) {
			System.out.println(bbNode.getDone()+"   "+bbNode.getMakespan());
		}
//		System.out.println(gen4.size());
		HibernateUtils.closeAll();
	}
	
	@Test 
	public void TestTree() {
		BBNode2 firstNode = new BBNode2(true);
		double upperBound = firstNode.getUpperBound();
		TreeSet<BBNode2> tree = new TreeSet<>();
		tree.add(firstNode);
		List<BBNode2> gen2 = firstNode.generate(upperBound);
		tree.addAll(gen2);
		System.out.println(tree.size());
		while(!tree.isEmpty()) {
			System.out.println(tree.pollFirst().getMakespan());
		}
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestBBalgorithm() {
		BB2 bba = new BB2();
		bba.algorithmRun(50000,2000);
		HibernateUtils.closeAll();
	}
}
