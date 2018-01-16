package cn.bottleneck.ga;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.utils.HibernateUtils;

import java.util.Set;

public class GA {
	/* GA �㷨���� */
	private static final double mutationRate = 0.015;
	private static final int newpopSize = 5;
	private static final boolean elitism = true;
	
	public static void main(String[] args) {
		GA ga = new GA();
		/* 
		 * ��һ��������ÿ����Ⱥ��С
		 * �ڶ��������Ƿ��ܴ���
		 * */
		ga.algorithmRun(50, 1000);
		HibernateUtils.closeAll();
	}
	
	public void algorithmRun(int populationNum,int genertations) {
		Population testga=new Population(populationNum,true);
		for(int i=0;i<genertations;i++)
		{
			testga = evolve(testga);
			System.out.print(i+1);System.out.println("	"+testga.getfittest().getMakespan());
		}
		
		Solution finalsolu=testga.getfittest();
		System.out.println("-------����---------");
		System.out.println(finalsolu.getMakespan());
	}
	
	//��������  Partial-Mapped Crossover (PMX)
	public Solution cross(Solution p1,Solution p2)
	{
		Solution child1 = new Solution();
		Solution child2 = new Solution();
		List<Integer> parent1 = p1.getA();
		List<Integer> parent2 = p2.getA();
		
		//ѡ��һ�������
		int posit = (int) (Math.random() * parent1.size());
		
		ArrayList<Integer> offspring1 = new ArrayList<>();
		ArrayList<Integer> offspring2 = new ArrayList<>();
		
		Map<Integer,Integer> map = new HashMap<>();
		
		ArrayList<Integer> temp1 = new ArrayList<>();
		ArrayList<Integer> temp2 = new ArrayList<>();
		
		//ǰ��θ��Ӵ�
		for(int i = 0;i<posit;i++) {
			offspring1.add(parent2.get(i));
			offspring2.add(parent1.get(i));
			map.put(parent2.get(i), parent1.get(i));
		}
		
		//����ӳ��
		Set<Entry<Integer, Integer>> entrySet = map.entrySet();
		for (Entry<Integer, Integer> entry : entrySet) {
			while(map.containsKey(entry.getValue())) {
				int value = entry.getValue();
				entry.setValue(map.get(value));
				map.put(value, -1);
			}
		}
		Iterator<Entry<Integer, Integer>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<Integer, Integer> entry = iterator.next();
			if(entry.getValue()==-1) {
				iterator.remove();
			}
		}
		
		//����
		for(int i = posit;i<parent1.size();i++) {
			temp1.add(parent1.get(i));
			temp2.add(parent2.get(i));
		}
		
		//��������ͻ��⣬������ӳ���޸�
		for(int i=0;i<temp1.size();i++) {
			int num = temp1.get(i);
			if(map.containsKey(num)) {
				temp1.set(i, map.get(num));
			}
		}
		
		for(int i=0;i<temp2.size();i++) {
			int num = temp2.get(i);
			if(map.containsValue(num)) {
				temp2.set(i, getKey(map,num));
			}
		}
		
		offspring1.addAll(temp1);
		offspring2.addAll(temp2);
		
		child1.setA(offspring1);
		child2.setA(offspring2);
		child1.caculateMakespan();
		child2.caculateMakespan();
		
		return child1.getfitness()>child2.getfitness()?child1:child2;
	}
	
	//��������
	public void mutate(Solution solut)
	{
		//����ÿһ������ÿһ�������п���ͻ��
		for(int i=0;i<solut.getA().size();i++)
		{
			//������ϱ��������������
			if(Math.random() < mutationRate)
			{
				//System.out.print("-------------ͻ�����λΪ----------��");System.out.println(i+1);		//������
				int j = (int) (Math.random() * solut.getA().size());
				//System.out.print("-------------��������λΪ----------��");System.out.println(j+1);		//������
				
				int temp = solut.getA().get(i);
				solut.getA().set(i,solut.getA().get(j));
				solut.getA().set(j,temp);
				solut.caculateMakespan();
			}
		}
	}
	
	//ѡ���ܵĸ���
	public Solution select(Population pop)
	{
		//����һ���յ���Ⱥ
		Population newpop = new Population(newpopSize, false);
		
		//���ѡ��pop�еĸ���
		for (int i = 0; i < newpopSize; i++) 
		{
			int randomId = (int) (Math.random() * pop.populationSize());
			newpop.solutions[i]= pop.solutions[randomId];
		}
		
		Solution fittest=newpop.getfittest();
		
		return fittest;
	}
	
	//������Ⱥ����
	public Population evolve(Population pop)
	{
		 Population newPopulation = new Population(pop.populationSize(), false);
		 
		 int elitismOffset = 0;
		 
		 //����һ�����Ÿ��壬�����ı䣬�����ڵ�һλ
		 if(elitism)
		 {
			 newPopulation.solutions[0]=pop.getfittest();
			 elitismOffset=1;
		 }
		 
		 for(int i=elitismOffset;i<newPopulation.populationSize();i++)
		 {
			 Solution parent1=select(pop);
			 Solution parent2=select(pop);
			 Solution child=cross(parent1,parent2);
			 newPopulation.solutions[i]=child;
		 }
		 
		 for(int i=elitismOffset;i<newPopulation.populationSize();i++)
		 {
			 mutate(newPopulation.solutions[i]);
		 }
		 return newPopulation;
	}
	
	private Integer getKey(Map<Integer,Integer> map,Integer value){  
        Integer key = null;  
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {  
            if(value.equals(entry.getValue())){  
                key=entry.getKey(); 
                break;
            }  
        }  
        return key;  
    }  
}
