package cn.AARun;

import org.junit.Test;

import cn.bottleneck.AfterBottleneck;
import cn.bottleneck.BeforeBottleneck;
import cn.bottleneck.ga.GA;
import cn.utils.HibernateUtils;
import cn.wholeplan.WholeScheduling;

public class Main {
/*	
 * ������ֺͳ���
 *	����ʹ�ø÷���ʱ�����Ƚ�jobitem��job����գ���
	*/
	@Test
	public void divideAndCombine() {
		BeforeBottleneck ab = new BeforeBottleneck();
		ab.divideAndCombine();
		HibernateUtils.closeAll();
		System.out.println("���������Ϲ������");
	}
	
	/*���ƿ�����Ŵ��㷨*/
	@Test
	public void GA() {
		GA ga = new GA();
		/* 
		 * ��һ��������ÿ����Ⱥ��С
		 * �ڶ��������Ƿ��ܴ���
		 * ���������������Ƿ���ʾ����ͼ
		 * ���ĸ����������Ƿ�������ݿ�
		 * */
		ga.algorithmRun(50, 500,true,true);
		HibernateUtils.closeAll();
	}
	
	/*
	 * �����������Ų�������ݿ�洢��������Ų���ɵ�jobתΪtask
	 * ����ʹ�ø÷���ʱ����֤taskΪ�գ����������ݿ����truncate table task����
	 */
	@Test
	public void afterBottleneck() {
		AfterBottleneck run = new AfterBottleneck();
		run.job2Task();
		System.out.println("���");
		HibernateUtils.closeAll();
	}
	
	/*
	 * ǰ�����Ƶ������Ų�
	 * ����ʹ�ø÷���ʱ����֤task����г��˵�ƣ�û����������
	 */
	@Test
	public void pushPull() {
		WholeScheduling ws = new WholeScheduling();
		ws.pushPull();
		System.out.println("���");
		HibernateUtils.closeAll();
	}
}
