package cn.AARun;

import org.junit.Test;

import cn.bottleneck.AfterBottleneck;
import cn.bottleneck.BeforeBottleneck;
import cn.bottleneck.ga.GA;
import cn.utils.HibernateUtils;
import cn.wholeplan.WholeScheduling;

public class Main {
/*	
 * 订单拆分和成组
 *	！！使用该方法时，需先将jobitem和job表清空！！
	*/
	@Test
	public void divideAndCombine() {
		BeforeBottleneck ab = new BeforeBottleneck();
		ab.divideAndCombine();
		HibernateUtils.closeAll();
		System.out.println("订单拆分与合挂已完成");
	}
	
	/*针对瓶颈的遗传算法*/
	@Test
	public void GA() {
		GA ga = new GA();
		/* 
		 * 第一个参数是每代种群大小
		 * 第二个参数是繁衍代数
		 * 第三个参数代表是否显示甘特图
		 * 第四个参数代表是否更新数据库
		 * */
		ga.algorithmRun(50, 500,true,true);
		HibernateUtils.closeAll();
	}
	
	/*
	 * 该类作用是排产后的数据库存储，将电镀排产完成的job转为task
	 * ！！使用该方法时，保证task为空，首先在数据库调用truncate table task！！
	 */
	@Test
	public void afterBottleneck() {
		AfterBottleneck run = new AfterBottleneck();
		run.job2Task();
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
	
	/*
	 * 前拉后推的整体排产
	 * ！！使用该方法时，保证task表格中除了电镀，没有其他！！
	 */
	@Test
	public void pushPull() {
		WholeScheduling ws = new WholeScheduling();
		ws.pushPull();
		System.out.println("完成");
		HibernateUtils.closeAll();
	}
}
