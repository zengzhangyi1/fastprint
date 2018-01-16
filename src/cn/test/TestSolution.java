package cn.test;

import org.junit.Test;

import cn.bottleneck.ga.Solution;

public class TestSolution {
	@Test
	public void TestRandom() {
		Solution solu = new Solution();
		solu.RandomSolution();
		System.out.println(solu.getA());
		System.out.println(solu.getA().size());
	}
	
	@Test
	public void TestCaculate() {
		Solution solu = new Solution();
		solu.RandomSolution();
		System.out.println(solu.getA());
		solu.caculateMakespan();
		System.out.println(solu.getMakespan());
	}
}
