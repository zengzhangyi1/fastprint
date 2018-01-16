package cn.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import cn.bottleneck.bb.BBNode;
import cn.bottleneck.bb.BB;
import cn.utils.HibernateUtils;

public class TestBBNode {
	@Test
	public void TestConstruction() {
		BBNode firstNode = new BBNode(true);
		Set<Double> keySet = firstNode.getUndo().keySet();
		for (Double double1 : keySet) {
			System.out.println(double1+":"+firstNode.getUndo().get(double1));
		}
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestcalculateMakespan() {
		BBNode node = new BBNode(true);
		ArrayList<Integer> done = new ArrayList<>();
//		done.add(4);
//		done.add(-4);
//		done.add(5);
//		
//		done.add(-5);
		node.setDone(done);
		System.out.println(node.caculateMakespan());
		/*ArrayList<Integer> undo = node.getUndo();
		for (Object object : undo) {
			System.out.println(object);
		}*/
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestgetUB() {
		BBNode node = new BBNode(true);
		System.out.println(node.getUpperBound());
		/*ArrayList<Integer> undo = node.getUndo();
		for (Object object : undo) {
			System.out.println(object);
		}*/
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestGenerate() {
		BBNode node = new BBNode(true);
		double upperBound = node.getUpperBound();
		List<BBNode> gen2 = node.generate(upperBound);
		List<BBNode> gen3 = gen2.get(0).generate(upperBound);
//		List<BBNode> gen4 = gen3.get(0).generate(upperBound);
//		double makespan4 = gen4.get(0).getMakespan(upperBound);
//		System.out.println(makespan4);
		for (BBNode bbNode : gen3) {
			System.out.println(bbNode.getDone()+"   "+bbNode.getMakespan());
		}
//		System.out.println(gen4.size());
		HibernateUtils.closeAll();
	}
	
	@Test 
	public void TestTree() {
		BBNode firstNode = new BBNode(true);
		double upperBound = firstNode.getUpperBound();
		TreeSet<BBNode> tree = new TreeSet<>();
		tree.add(firstNode);
		List<BBNode> gen2 = firstNode.generate(upperBound);
		tree.addAll(gen2);
		System.out.println(tree.size());
		while(!tree.isEmpty()) {
			System.out.println(tree.pollFirst().getMakespan());
		}
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestBBalgorithm() {
		BB bba = new BB();
		bba.algorithmRun(50000,2000);
		HibernateUtils.closeAll();
	}
}
