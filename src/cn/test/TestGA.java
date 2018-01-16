package cn.test;

import java.util.Arrays;

import org.junit.Test;

import cn.bottleneck.ga.GA;
import cn.bottleneck.ga.Solution;
import cn.utils.HibernateUtils;

public class TestGA {
	@Test
	public void TestCross() {
		Integer a1[] = {3,4,5,6,7,8,9,1,2};
		Integer a2[] = {6,9,2,1,7,8,3,5,4};
		
		Solution p1 = new Solution();
		Solution p2 = new Solution();
		
		p1.setA(Arrays.asList(a1));
		p2.setA(Arrays.asList(a2));
		
		GA ga = new GA();
		Solution list = ga.cross(p1, p2);
		System.out.println(list.getA());
		System.out.println(list.getMakespan());
		HibernateUtils.closeAll();
	}
	
	@Test
	public void TestMutate() {
		Integer a1[] = {3,4,5,6,7,8,9,1,2};
		
		Solution p1 = new Solution();
		
		p1.setA(Arrays.asList(a1));
		
		GA ga = new GA();
		System.out.println("变异前："+p1.getA());
		ga.mutate(p1);
		System.out.println("变异后："+p1.getA());
	}
}
