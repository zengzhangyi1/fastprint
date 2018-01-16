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
	/* GA 算法参数 */
	private static final double mutationRate = 0.015;
	private static final int newpopSize = 5;
	private static final boolean elitism = true;
	
	public static void main(String[] args) {
		GA ga = new GA();
		/* 
		 * 第一个参数是每代种群大小
		 * 第二个参数是繁衍代数
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
		System.out.println("-------最优---------");
		System.out.println(finalsolu.getMakespan());
	}
	
	//交叉算子  Partial-Mapped Crossover (PMX)
	public Solution cross(Solution p1,Solution p2)
	{
		Solution child1 = new Solution();
		Solution child2 = new Solution();
		List<Integer> parent1 = p1.getA();
		List<Integer> parent2 = p2.getA();
		
		//选择一个交叉点
		int posit = (int) (Math.random() * parent1.size());
		
		ArrayList<Integer> offspring1 = new ArrayList<>();
		ArrayList<Integer> offspring2 = new ArrayList<>();
		
		Map<Integer,Integer> map = new HashMap<>();
		
		ArrayList<Integer> temp1 = new ArrayList<>();
		ArrayList<Integer> temp2 = new ArrayList<>();
		
		//前半段给子代
		for(int i = 0;i<posit;i++) {
			offspring1.add(parent2.get(i));
			offspring2.add(parent1.get(i));
			map.put(parent2.get(i), parent1.get(i));
		}
		
		//配置映射
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
		
		//后半段
		for(int i = posit;i<parent1.size();i++) {
			temp1.add(parent1.get(i));
			temp2.add(parent2.get(i));
		}
		
		//后半段做冲突检测，并根据映射修改
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
	
	//变异算子
	public void mutate(Solution solut)
	{
		//遍历每一个基因，每一个基因都有可能突变
		for(int i=0;i<solut.getA().size();i++)
		{
			//如果符合变异条件，则变异
			if(Math.random() < mutationRate)
			{
				//System.out.print("-------------突变基因位为----------：");System.out.println(i+1);		//测试用
				int j = (int) (Math.random() * solut.getA().size());
				//System.out.print("-------------交换基因位为----------：");System.out.println(j+1);		//测试用
				
				int temp = solut.getA().get(i);
				solut.getA().set(i,solut.getA().get(j));
				solut.getA().set(j,temp);
				solut.caculateMakespan();
			}
		}
	}
	
	//选择繁衍的个体
	public Solution select(Population pop)
	{
		//建立一个空的种群
		Population newpop = new Population(newpopSize, false);
		
		//随机选择pop中的个体
		for (int i = 0; i < newpopSize; i++) 
		{
			int randomId = (int) (Math.random() * pop.populationSize());
			newpop.solutions[i]= pop.solutions[randomId];
		}
		
		Solution fittest=newpop.getfittest();
		
		return fittest;
	}
	
	//进化种群函数
	public Population evolve(Population pop)
	{
		 Population newPopulation = new Population(pop.populationSize(), false);
		 
		 int elitismOffset = 0;
		 
		 //保持一个最优个体，不作改变，并放在第一位
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
