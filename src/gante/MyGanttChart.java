package gante;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;


public class MyGanttChart extends JFrame{
	private int N;                    //工件数
	private int M;
	private int maxm;                //实际最大机器数
	private int sum;                 //总工序数
	private int process[];
	private int processID[];
	private int machine[];
	private int SE[];
	private int CE[];
	private int SL[];
	private int CL[];
	private int T[][];
	
	
	private int makespan;
	
	private Shape[] rectProcess;
	private Shape[] rectMachine;
	private Shape[] rectScale;
	private Shape rectY;
	private Shape rectMakespan;
	private Shape rectIdle[][];
	private Shape rectLable;
	Font font;
	Font minfont;
	
	private String machineName[];
	private String processName[];
	
	private int unitHeight = 20;
	private int inteval = 10;
	private int xproportion = 16;      //
	private int uniteX = 5;
	private int XMax;             //
	private int YMax;             //
	private int Ystart = 12;
	private int Xstart = 60;
	
	private int pj[];
	private int pm[];
	private int sj[];
	private int sm[];
	private int st[];
	private int ct[];
	private int t[];
	private boolean key[];                //key[i]为true表示工序i为关键工序
	private boolean flag[];
	private boolean haveKey[];     //haveKey[i]为true表示机器i加工了关键工序
	
	private int num[];              //num[i]记录机器i加工工序的数量
	private int processedJob[][];   //processedJob[i][j]:机器i加工的第j道工序
	private int MIT[];
	private int Idle[][];            //怠机时间
	private int IdleStart[][];       //记录怠机开始时间
	private int totalIdleTime;       //总怠机时间
	private int INF = 100000;
	
	private static final Random rand = new Random();   //创建随机对象
	
	public static final Color BACKGROUND = new Color(180, 180, 255);
	public static final Color PURPLE = new Color(128,0,128);             //紫色/
	public static final Color DeepSkyBlue = new Color(0,191,255);       //深天蓝/
	public static final Color Auqamarin = new Color(127,255,170);       //碧绿色/
	public static final Color GreenYellow = new Color(173,255,47);      //绿黄色/
	public static final Color Olive = new Color(128,128,0);              //橄榄色/
	public static final Color Coral = new Color(255,127,80);             //珊瑚色/
	public static final Color LightSkyBlue = new Color(135,206,250);    //淡蓝色
	Color[] col0 = new Color[]{Auqamarin,PURPLE,Color.BLUE,Color.CYAN,Color.GREEN,DeepSkyBlue,Color.YELLOW,Color.GRAY,Color.PINK,Coral,Olive,Color.MAGENTA,GreenYellow};
	Color[] col = new Color[30];
	
	public MyGanttChart(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("N3M3.txt"));
			
			int index = 0;
			int i = 0, j = 0;
			String temp;       //
			//ary[0]：工件数； ary[1]：机器数；sum：工序数？
			while((temp = br.readLine())!=null){
				int[] ary = aryChange(temp);  //
				if(index == 0){
					N = ary[0];
					M = ary[1];
					sum = ary[2];
					T = new int[sum][M];
				}
				else{
					T[i++] = ary;
				}
				index++;
			}
			br.close();
			
			for(i=0; i<col0.length; i++){
				col[i] = col0[i];
			}
			for(i=col0.length; i<30; i++){
				int r = rand.nextInt(255);
				int y = rand.nextInt(255);
				int g = rand.nextInt(255);
				Color b = new Color(r, y, g);
				col[i] = b;
			}
			
			process = new int[sum];
			processID = new int[sum];
			machine = new int[sum];
			BufferedReader br2 = new BufferedReader(new FileReader("rnm.txt"));
			index = 0;
			while((temp = br2.readLine())!=null){
				int[] ary = aryChange(temp);  //
				if(index == 0){
					process = ary;
				}
				else if(index == 1){
					processID = ary;
				}
				else if(index == 2){
					machine = ary;
				}
				index++;
			}
			br2.close();
			maxm = 0;
			for(i=0; i<sum; i++){
				if(machine[i] > maxm)
					maxm = machine[i];
			}
			maxm = maxm + 1;
			maxm = M;
			
			SE = new int[sum];
			CE = new int[sum];
			SL = new int[sum];
			CL = new int[sum];
			int[] PJ = new int[N];
			int[] PM = new int[M];
			int[] SJ = new int[N];
			int[] SM = new int[M];
			Arrays.fill(SE, 0);
			Arrays.fill(CE, 0);
			Arrays.fill(PJ, 0);
			Arrays.fill(PM, 0);
			
			for (i=0; i<sum; i++){
				//System.out.println(process[i]);
		        SE[processID[i]] = (PJ[process[i]] > PM[machine[i]] ? PJ[process[i]] : PM[machine[i]]);
		        CE[processID[i]] = SE[processID[i]]+T[processID[i]][machine[i]];
		        PJ[process[i]] = CE[processID[i]];
		        PM[machine[i]] = CE[processID[i]];
		        //System.out.print(SE[processID[i]] + " ");
			}
			//System.out.print("\n");
			//for (i=0; i<sum; i++){
			//	System.out.print(CE[processID[i]] + " ");
			//}
			makespan = 0;
			for (i=0; i<M; i++){
			    if (makespan < PM[i])
			        makespan = PM[i];
			}
			
			//计算最晚开始时间和最晚结束时间
			Arrays.fill(SJ, makespan);
			Arrays.fill(SM, makespan);
			for (i=sum-1; i>=0; i--){
		        CL[processID[i]] = (SJ[process[i]] < SM[machine[i]]) ? SJ[process[i]] : SM[machine[i]];
		        SL[processID[i]] = CL[processID[i]]-T[processID[i]][machine[i]];
		        SJ[process[i]] = SL[processID[i]];
		        SM[machine[i]] = SL[processID[i]];
			}
			
			key = new boolean[sum];
			Arrays.fill(key, false);
			for(i=0; i<sum; i++){
				if(SE[processID[i]] == SL[processID[i]])
					key[processID[i]] = true;
			}
			
			pj = new int[sum];
			pm = new int[sum];
			sj = new int[sum];
			sm = new int[sum];
			st = new int[sum];
			ct = new int[sum];
			t = new int[sum];
			num = new int[M];
			flag = new boolean[sum];
			processedJob = new int[M][];
			Idle = new int[M][];
			IdleStart = new int[M][];
			rectIdle = new Shape[M][];
			haveKey = new boolean[M];
			MIT = new int[M];
			getIdleTime();
			
			machineName = new String[maxm];
			for(i=0; i<maxm; i++){
				machineName[i] = "M" + (i + 1);
			}
			
			int[] JobTimes = new int[N];
			processName = new String[sum];
			Arrays.fill(JobTimes, 0);
			for (i=0; i<sum; i++){
				int job = process[i];
				int times = JobTimes[job]++;
				//processName[i] = "(" + job + "," + times + ")";
				processName[i] = "" + job;
			}
			rectY = new Rectangle2D.Double(0, Ystart, Xstart, unitHeight);
			rectMachine = new Shape[maxm];
			for(i=0; i<maxm; i++){
				rectMachine[i] = new Rectangle2D.Double(Xstart-32, Ystart + (i + 1)*(unitHeight + inteval), 32, unitHeight);
			}
			
			rectProcess = new Shape[sum];
			
			/*for(i=0; i<sum; i++){
				int x = Xstart + SE[processID[i]] * xproportion;
				int y = Ystart + (machine[i] + 1)*(unitHeight + inteval);
				int length = (CE[processID[i]] - SE[processID[i]]) * xproportion;
				rectProcess[i] = new Rectangle2D.Double(x, y, length, unitHeight);
			}*/
			
			for(i=0; i<sum; i++){
				int x = Xstart + st[i] * xproportion;
				int y = Ystart + (machine[i] + 1)*(unitHeight + inteval);
				int length = t[i] * xproportion;
				rectProcess[i] = new Rectangle2D.Double(x, y, length, unitHeight);
			}
			
			for(i=0; i<M; i++){
			    for(j=0; j<num[i]-1; j++){
			    	int x = Xstart + IdleStart[i][j] * xproportion;
			    	int y = Ystart + (i + 1)*(unitHeight + inteval);
			    	int length = Idle[i][j] * xproportion;
			    	rectIdle[i][j] = new Rectangle2D.Double(x, y, length, unitHeight);
			    }	
			}
			
			int numScale = makespan / uniteX + 2;
			rectScale = new Shape[makespan / uniteX + 2];
			for(i=0; i<numScale; i++){
				int width = 40;
				int x = Xstart + i*uniteX*xproportion - width / 2;
				int y = Ystart + (maxm + 1)*(unitHeight + inteval);
				rectScale[i] = new Rectangle2D.Double(x, y, width, width);
			}
			
			int width = 40;
			int x = Xstart + makespan*xproportion;
			int y = Ystart + (maxm - 2)*(unitHeight + inteval);
			rectMakespan = new Rectangle2D.Double(x, y, width, width);
			
			x = Xstart;
			y = (maxm + 2) * (unitHeight + inteval) + Ystart;
			int length = (numScale - 1)*uniteX*xproportion;
			rectLable = new Rectangle2D.Double(x, y, length, unitHeight);
			
			YMax = (maxm + 5) * (unitHeight + inteval) + Ystart;
			XMax = (makespan / uniteX + 3) * uniteX * xproportion + Xstart;
			
			
			this.setSize(XMax,YMax);
			this.setTitle("甘特图");
			addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowOpened(WindowEvent e) {
	                do_this_windowOpened(e);
	            }
	        });
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			font = new Font("宋体",Font.BOLD,16);
			minfont = new Font("宋体",Font.BOLD,10);
			
			final BufferedImage targetImg = new BufferedImage(XMax-150, YMax, BufferedImage.TYPE_INT_RGB);//
			final Graphics2D g2d = targetImg.createGraphics();
			g2d.setBackground(Color.WHITE); 
			//drawGanttChart(g2d);
			
			File f=new File("gantt.jpg");
			if(!f.exists()){
				try{
					f.createNewFile();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			try{
				FileOutputStream fos=new FileOutputStream(f);
				ImageIO.write(targetImg, "JPEG", fos);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			add(new CanvasPanel());
			
			System.out.println("makespan=" + makespan);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void main(String args[]){
		new MyGanttChart().setVisible(true);
	}
	
	/*正则表达式中\s匹配任何空白字符，包括空格、制表符、换页符
	 * +  表示一个或多个 
	 * */
	static int[] aryChange(String temp){
		String[] ss = temp.trim().split("\\s+");
		                                        
		int[] ary = new int[ss.length];
		int i = 0;
		for(String str:ss){
			ary[i++] = Integer.parseInt(str);
		}
		return ary;
	}
	
	public void getSchedule(){
		int i,j;
		Arrays.fill(pj, -1);
		Arrays.fill(pm, -1);
		Arrays.fill(sj, -1);
		Arrays.fill(sm, -1);
		Arrays.fill(num, 0);
		for(i=0; i<sum; i++){
			st[i] = SE[processID[i]];
			ct[i] = CE[processID[i]];
			t[i] = T[processID[i]][machine[i]];
			if(key[processID[i]]){
				flag[i] = true;
			}else{
				flag[i] = false;
			}
		}
		//确定pj,sj
		for(i=1; i<sum; i++){
			if(pj[i] == -1){
				for(j=i-1; j>=0; j--){
					if(process[i] == process[j]){
						pj[i] = j;
						sj[j] = i;
						break;
					}
				}
			}
		}
		//确定pm,sm
		for(i=1; i<sum; i++){
			for(j=i-1; j>=0; j--){
				if(machine[i] == machine[j]){
					pm[i] = j;
					sm[j] = i;
					break;
				}
			}		}
		
		//Arrays.fill(num, 0);
		for(i=0; i<sum; i++){
			num[machine[i]]++;
		}
		for(i=0; i<maxm; i++){
			processedJob[i] = new int[num[i]];
			if(num[i] - 1 > 0){
				Idle[i] = new int[num[i]-1];
				IdleStart[i] = new int[num[i]-1];
				rectIdle[i] = new Shape[num[i] - 1];
			}
			
		}
		for(i=0; i<maxm; i++){
			int k = 0;
			for(j=0; j<sum; j++){
				if(machine[j] == i){
					processedJob[i][k++] = j;
				}
			}
		}
	}
	
	public void getIdleTime(){
		int i,j;
		Arrays.fill(MIT, 0);
		getSchedule();
		//moveRight();
		moveRight2();
		for(i=0; i<maxm; i++){
			for(j=1; j<processedJob[i].length; j++){
				IdleStart[i][j-1] = st[processedJob[i][j-1]] + t[processedJob[i][j-1]];
				Idle[i][j-1] = st[processedJob[i][j]] - (st[processedJob[i][j-1]] + t[processedJob[i][j-1]]);
				//Idle[i][j-1] = work[processedJob[i][j]].st - (work[processedJob[i][j-1]].st + work[processedJob[i][j-1]].t);
				MIT[i] += Idle[i][j-1];
			}
		}
		for(i=0; i<maxm; i++){
			if(num[i] - 1 > 0){
				for(j=0; j<Idle[i].length; j++){
					if(Idle[i][j]!=0)
					    System.out.print(Idle[i][j] + " ");
				}
			}
			
			System.out.print("\n");
		}
		totalIdleTime = 0;
		for(i=0; i<M; i++){
			totalIdleTime += MIT[i];
		}
		System.out.println("totalIdleTime = " + totalIdleTime);
	}
	
	public void moveRight(){                             //没有加工关键工序的机器上的最后一道工序固定
		int i, j;
		Arrays.fill(haveKey, false);                      //初始化为false                    
		for(i=0; i<maxm; i++){
			for(j=0; j<processedJob[i].length; j++){
				if(key[processID[processedJob[i][j]]]){
					haveKey[i] = true;
					break;
				}
			}
		}
		for(i=0; i<maxm; i++){
			System.out.print(haveKey[i] + " ");
		}
		
		for(i=0; i<maxm; i++){
			if(haveKey[i]){
				for(j=processedJob[i].length-1; j>=0; j--){
					if(key[processID[processedJob[i][j]]]) break;
					flag[processedJob[i][j]] = true;                       //同一机器上关键工序后面的工序固定不动             
				}
			}
			else{
				flag[processedJob[i][processedJob[i].length-1]] = true;    //
			}
			
		}
		
		for(i=sum-1; i>=0; i--){            //从后往前遍历
			if(flag[i] == false){         //当前工序位置没有固定，可以移动
				
				int tj = sj[i] == -1 ? INF : st[sj[i]];          //工件后继开始时间
				int tm = sm[i] == -1 ? INF : st[sm[i]];          //机器后继开始时间
				ct[i] = tj < tm ? tj : tm;                                 //当前工序完工时间为两者较小值
				st[i] = ct[i] - t[i];
			}
		}
	}
	
	public void moveRight2(){
		int i, j;
		Arrays.fill(haveKey, false);                      //初始化为false                    
		for(i=0; i<maxm; i++){
			for(j=0; j<processedJob[i].length; j++){
				if(key[processID[processedJob[i][j]]]){
					haveKey[i] = true;
					break;
				}
			}
		}
		for(i=0; i<maxm; i++){
			System.out.print(haveKey[i] + " ");
		}
		for(i=0; i<maxm; i++){
			for(j=processedJob[i].length-1; j>=0; j--){
				if(key[processID[processedJob[i][j]]]) break;
				if(haveKey[i])
				    flag[processedJob[i][j]] = true;
			}
		}
		
		for(i=sum-1; i>=0; i--){            //从后往前遍历
			if(!flag[i] && haveKey[machine[i]]){         //当前工序位置没有固定，可以移动
				//System.out.print(i + " ");
				int tj = sj[i] == -1 ? INF : st[sj[i]];          //工件后继开始时间
				int tm = sm[i] == -1 ? INF : st[sm[i]];          //机器后继开始时间
				if(sj[i]!=-1 && !haveKey[machine[sj[i]]]){
					//System.out.println("i="+i);
					if(tj < tm){
						int tjj = sj[sj[i]] == -1 ? INF : st[sj[sj[i]]];
						int tmm = sm[sj[i]] == -1 ? INF : st[sm[sj[i]]];        //不要忘了这一条
						tjj = tjj < tmm ? tjj : tmm;
						ct[sj[i]] = (tm + t[sj[i]]) < tjj ? (tm + t[sj[i]]) : tjj;
						tj = st[sj[i]] = ct[sj[i]] - t[sj[i]];
					}
					haveKey[machine[sj[i]]] = true;
					flag[sj[i]] = true;
					ct[i] = tj < tm ? tj : tm;                                 //当前工序完工时间为两者较小值
					st[i] = ct[i] - t[i];
					for(int k=processedJob[machine[sj[i]]].length-1; k>=0; k--){
						if(processedJob[machine[sj[i]]][k] == sj[i]) break;
						flag[processedJob[machine[sj[i]]][k]] = true;
					}
					i = sj[i];
					//System.out.println("i="+i);
				}
				else{
					ct[i] = tj < tm ? tj : tm;                                 //当前工序完工时间为两者较小值
					st[i] = ct[i] - t[i];
				}
				//for(int k=0; k<maxm; k++){
				//	System.out.print(haveKey[k] + " ");
				//}
			}
			
			
		}
		
		
		
	}
	
	
	
	class CanvasPanel extends Canvas{
		public void paint(Graphics g){
			super.paint(g);
			Graphics2D g2 = (Graphics2D)g;
			
			Shape backGround;
			backGround = new Rectangle2D.Double(0, 0, XMax, YMax);
			g2.setColor(Color.WHITE);
			g2.fill(backGround);
			g2.setFont(font);
			
			for(int i=0; i<maxm; i++){
				g2.setColor(BACKGROUND);
				g2.fill(rectMachine[i]);
				g2.setColor(Color.BLACK);
				g2.draw(rectMachine[i]);
				TextUtilities.paintString(g2, machineName[i], 
						rectMachine[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			}
			for(int i=0; i<sum; i++){
				g2.setColor(col[process[i]]);
				g2.fill(rectProcess[i]);
				
				g2.setColor(Color.BLACK);
				g2.draw(rectProcess[i]);
				if(key[processID[i]]){
					g2.setColor(Color.RED);
				}else{
					g2.setColor(Color.BLACK);
				}
				
				TextUtilities.paintString(g2, processName[i], 
						rectProcess[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			}
			g2.setFont(font);
			
			g2.setColor(Color.BLACK);
			TextUtilities.paintString(g2, "机器", 
					rectY.getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			g2.setFont(font);

			for(int i=0; i<rectScale.length; i++){
				//g2.fill(rectScale[i]);
				TextUtilities.paintString(g2, Integer.toString(i*uniteX), 
						rectScale[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			}
			
			TextUtilities.paintString(g2, Integer.toString(makespan), 
					rectMakespan.getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			int y = Ystart + (maxm + 1)*(unitHeight + inteval);
			int x = (makespan / uniteX + 1) * uniteX * xproportion + Xstart;
			g2.setColor(Color.BLACK);
			g2.drawLine(Xstart, y, x, y);
			int[] xs = {Xstart, Xstart-3, Xstart+3};
			int[] ys = {Ystart, Ystart+10, Ystart+10};
			g2.fillPolygon(xs,ys,3);
			g2.drawLine(Xstart,Ystart, Xstart, y);
			for(int i=0; i<(makespan / uniteX + 1); i++){
				g2.drawLine(Xstart + (i+1)*uniteX*xproportion, y, Xstart + (i+1)*uniteX*xproportion, y-5);
			}
			
			int y0 = Ystart + (unitHeight + inteval);
			int x0 = Xstart + makespan*xproportion;
			g2.drawLine(x0, y0, x0, y);
			g2.drawLine(Xstart, y0, x0, y0);
			
			TextUtilities.paintString(g2, "makespan = "+makespan +" ; totalIdleTime = "+totalIdleTime, 
					rectLable.getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
			
			for(int i=0; i<M; i++){
				for(int j=0; j<num[i]-1; j++){
					g2.draw(rectIdle[i][j]);
					TextUtilities.paintString(g2, Integer.toString(Idle[i][j]), 
							rectIdle[i][j].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
				}
			}
			for(int i=0; i<M; i++){
				for(int j=0; j<num[i]-1; j++){
					drawSelPolygon(g2,Color.BLACK,rectIdle[i][j]);        //用斜线填充
				}
			}
		}
	}
	
	public static void drawSelPolygon(Graphics2D g2, Color frameColor, Shape a) {
	    g2.setColor(frameColor);
	    //初始化多边形
	    //取得多边形外接矩形
	    Rectangle r = a.getBounds();
	    //裁切
	    g2.setClip(a);
	    //绘制填充线
	    for (int j = r.y; j-r.width < r.y + r.height; j = j + 6) {
	        //<--NG
	        Line2D line = new Line2D.Float(r.x, j, (r.x + r.width), j-r.width);
	        g2.draw(line);
	    }
	    //绘制多边形
	    //g2.draw(a);
	}
	
	/*private void drawGanttChart(Graphics2D g2){
		//g2.setBackground(Color.WHITE); 
		Shape backGround;
		backGround = new Rectangle2D.Double(0, 0, XMax, YMax);
		g2.setColor(Color.WHITE);
		g2.fill(backGround);
		g2.setFont(font);
		
		for(int i=0; i<maxm; i++){
			g2.setColor(BACKGROUND);
			g2.fill(rectMachine[i]);
			g2.setColor(Color.BLACK);
			g2.draw(rectMachine[i]);
			TextUtilities.paintString(g2, machineName[i], 
					rectMachine[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
		}
		//g2.setFont(minfont);
		for(int i=0; i<sum; i++){
			g2.setColor(col[process[i]]);
			g2.fill(rectProcess[i]);
			if(key[i]){
				g2.setColor(Color.RED);
			}else{
				g2.setColor(Color.BLACK);
			}
			g2.draw(rectProcess[i]);
			TextUtilities.paintString(g2, processName[i], 
					rectProcess[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
		}
		g2.setFont(font);
		
		for(int i=0; i<M; i++){
			for(int j=0; j<num[i]-1; j++){
				g2.setColor(Color.BLACK);
				g2.fill(rectIdle[i][j]);
			}
		}
		
		g2.setColor(Color.BLACK);
		TextUtilities.paintString(g2, "", 
				rectY.getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
		g2.setFont(font);

		for(int i=0; i<rectScale.length; i++){
			//g2.fill(rectScale[i]);
			TextUtilities.paintString(g2, Integer.toString(i*5), 
					rectScale[i].getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
		}
		
		TextUtilities.paintString(g2, Integer.toString(makespan), 
				rectMakespan.getBounds(), TextUtilities.CENTER, TextUtilities.CENTER);
		int y = Ystart + (maxm + 1)*(unitHeight + inteval);
		int x = (makespan / uniteX + 1) * uniteX * xproportion + Xstart;
		g2.setColor(Color.BLACK);
		g2.drawLine(Xstart, y, x, y);
		int[] xs = {Xstart, Xstart-3, Xstart+3};
		int[] ys = {Ystart, Ystart+10, Ystart+10};
		g2.fillPolygon(xs,ys,3);
		g2.drawLine(Xstart,Ystart, Xstart, y);
		for(int i=0; i<(makespan / uniteX + 1); i++){
			g2.drawLine(Xstart + (i+1)*uniteX*xproportion, y, Xstart + (i+1)*uniteX*xproportion, y-5);
		}
		
		int y0 = Ystart + (1)*(unitHeight + inteval);
		int x0 = Xstart + makespan*xproportion;
		g2.drawLine(x0, y0, x0, y);
	}*/
	
	protected void do_this_windowOpened(WindowEvent e) {
        setLocationRelativeTo(null);// 
    }
}

