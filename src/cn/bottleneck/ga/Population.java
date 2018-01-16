package cn.bottleneck.ga;

public class Population {
	public Solution [] solutions;
	
	public Population(int size,boolean initialise)	//initialise���Ƿ���Ҫ��ʼ��
	{
		solutions=new Solution[size];
		if(initialise)
		{
			for(int i=0;i<size;i++)
			{
				Solution newsolut=new Solution();
				newsolut.RandomSolution();
				newsolut.caculateMakespan();
				solutions[i]=newsolut;
			}
		}
	}
	
	public int populationSize()
	{
		return solutions.length;
	}
	//ѡȡ���ŵĸ���
	public Solution getfittest()
	{
		Solution fittest=solutions[0];
		for(int i=0;i<solutions.length;i++)
		{
			if(fittest.getfitness()<solutions[i].getfitness()) fittest=solutions[i];
		}
		return fittest;
	}
}
