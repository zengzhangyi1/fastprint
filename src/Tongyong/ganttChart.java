/**
 * 
 */
package Tongyong;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JFrame;
import gante.TextUtilities;
@SuppressWarnings("serial")
public class ganttChart extends JFrame {
	private int macNum = 15;// 机器数
	private int makespan = 352;
	private int rows = 100;// 读取数据的时候用到，test.txt中数据的行数
	private Map<Integer, List<Integer>> begin = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> end = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> jobIndex = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> gongxuIndex = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> guanjiangx = new HashMap<>(macNum);

	private int unitHeight = 20;
	private int macLen = 30;// 机器方块的长度
	private int interval = 20;
	private float xproportion = 2.5f;
	private float XMax; //
	private int YMax;
	private int Ystart = 12; // Ystart Xstart 定义为M1矩形框的右上角坐标
	private int Xstart = 35;

	private Shape[] rectMachine;// y轴边上的 机器矩形
	private Map<Integer, List<Shape>> rectProcess = new HashMap<>(macNum);
	private Map<Integer, List<Shape>> rectStrFt = new HashMap<>(macNum);// 帮助标注完工时间的矩形块
	private Map<Integer, List<Shape>> rectStrSt = new HashMap<>(macNum);// 帮助标注开始加工时间的矩形块
	private Map<Integer, List<Shape>> rectSetup = new HashMap<>(macNum);// 准换时间的矩形块
	Font font; // rectProcess矩形框上的文字
	Font font2;// rectStrFt矩形框上的文字

	private String machineName[];

	private static final Random rand = new Random(); // 创建随机对象
	public static final Color BACKGROUND = new Color(180, 180, 255);
	public static final Color PURPLE = new Color(249, 204, 226); // 粉色/
	public static final Color DeepSkyBlue = new Color(250, 250, 250); // 天蓝/
	public static final Color Auqamarin = new Color(0, 255, 127); // 碧绿色/
	public static final Color GreenYellow = new Color(173, 255, 47); // 绿黄色/
	public static final Color Olive = new Color(128, 128, 0); // 橄榄色/
	public static final Color Coral = new Color(255, 222, 173); // 珊瑚色/
	public static final Color LightSkyBlue = new Color(135, 206, 250); // 淡蓝色
	Color[] col0 = new Color[] { Auqamarin, PURPLE, GreenYellow, Color.MAGENTA, Color.GREEN, Color.YELLOW, Color.GRAY,
			Color.PINK, Coral, Olive, Color.CYAN };
	Color[] col = new Color[30];

	public ganttChart() {
		try {
			int i = 0, s = 0;
			for (i = 0; i < col0.length; i++) {
				col[i] = col0[i];
			}
			int r;
			int g;
			int b;
			for (i = col0.length; i < 30; i++) {
				while (true)
					if ((r = rand.nextInt(255)) > 50)
						break;
				while (true)
					if ((g = rand.nextInt(255)) > 50)
						break;
				while (true)
					if ((b = rand.nextInt(255)) > 50)
						break;
				Color c = new Color(r, g, b);
				col[i] = c;
			}

			machineName = new String[macNum];
			for (i = 0; i < macNum; i++) {
				machineName[i] = "M" + (i + 1);
			}
			readData2();

			int size, beginTmp, endTmp;
			float x, y, length, x1, y1, len, x11;
			for (i = 0; i < macNum; i++) {
				if (begin.get(i).size() == 0)
					continue;
				size = begin.get(i).size();
				List<Shape> rectProcessList = new ArrayList<>();
				List<Shape> rectStrFtList = new ArrayList<>();
				List<Shape> rectStrStList = new ArrayList<>();
				List<Shape> rectSetupList = new ArrayList<>();
				for (s = 0; s < size; s++) {
					beginTmp = begin.get(i).get(s);
					endTmp = end.get(i).get(s);
					x = Xstart + beginTmp * xproportion;
					y = Ystart + i * (unitHeight + interval);
					length = (endTmp - beginTmp) * xproportion;
					rectProcessList.add(new Rectangle2D.Double(x, y, length, unitHeight));

					// 下面定义 帮助标注完工时间的矩形块
					x1 = Xstart + endTmp * xproportion;
					y1 = Ystart + (i + 1) * unitHeight + i * interval;
					len = 30;
					rectStrFtList.add(new Rectangle2D.Double(x1 - len / 2, y1, len, interval));

					// 下面定义 帮助标注开始加工时间的矩形块
					x11 = Xstart + beginTmp * xproportion;
					rectStrStList.add(new Rectangle2D.Double(x11 - len / 2, y1, len, interval));

				}
				rectProcess.put(i, rectProcessList);
				rectStrFt.put(i, rectStrFtList);
				rectStrSt.put(i, rectStrStList);
				rectSetup.put(i, rectSetupList);
			}

			rectMachine = new Shape[macNum];
			for (i = 0; i < macNum; i++)
				rectMachine[i] = new Rectangle2D.Double(Xstart - macLen, Ystart + i * (unitHeight + interval), macLen,
						unitHeight);

			XMax = makespan * xproportion + 2 * Xstart;
			YMax = (macNum + 3) * (unitHeight + interval) + Ystart;

			this.setSize((int) XMax, YMax);
			this.setTitle("甘特图");
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					do_this_windowOpened(e);
				}
			});
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			font = new Font("宋体", Font.BOLD, 12);
			font2 = new Font("宋体", Font.BOLD, 12);
			add(new CanvasPanel());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void mapMacI_StageI(int stageNum, HashMap<Integer, Integer> map, Integer[] mac) {

		int count; // 存储该阶段之前所有阶段总共的机器数
		for (int s = 0; s < stageNum; s++) {
			count = 0;
			for (int c = 0; c < s; c++)
				count += mac[c];
			for (int m = count; m < count + mac[s]; m++)
				map.put(m, s);
		}

	}
// <2>读取数据 格式 test工件号0 工序号1 开始时间2 结束时间3 机床号4 是否是关键工序5
	public void readData2() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("tongyong.txt"));
		Integer[][] arr = new Integer[rows][6];
		int index = 0;
		String temp;
		while ((temp = br.readLine()) != null) {
			arr[index++] = aryChange(temp);
		}
		br.close();

		// 初始化
		for (int i = 0; i < macNum; i++) {
			begin.put(i, new ArrayList<Integer>());
			end.put(i, new ArrayList<Integer>());
			jobIndex.put(i, new ArrayList<Integer>());
			gongxuIndex.put(i, new ArrayList<Integer>());
			guanjiangx.put(i, new ArrayList<Integer>());
		}

		// 工件号0 工序号1 开始时间2 结束时间3 机床号4 是否是关键工序5
		// （test.txt中工件，工序，机床序号均从1/0开始）
		int macTmp;
		for (int i = 0; i < rows; i++) {
			
//			/*---------工件、机床、工序号从0开始--------------------*/
//			macTmp = arr[i][4];
//			jobIndex.get(macTmp).add(arr[i][0]);
//			gongxuIndex.get(macTmp).add(arr[i][1]);
			
			/*---------工件、机床、工序号从1开始--------------------*/
			macTmp = arr[i][4]-1;
			jobIndex.get(macTmp).add(arr[i][0]-1);
			gongxuIndex.get(macTmp).add(arr[i][1]-1);

			begin.get(macTmp).add(arr[i][2]);
			end.get(macTmp).add(arr[i][3]);
			guanjiangx.get(macTmp).add(arr[i][5]);
		}
	}
	// 正则表达式中\s匹配任何空白字符，包括空格、制表符、换页符 + 表示一个或多个
	static Integer[] aryChange(String temp) {
		String[] ss = temp.trim().split("\\s+");

		Integer[] ary = new Integer[ss.length];
		int i = 0;
		if (!"".equals(ss[0])) {
			for (String str : ss) {
				ary[i++] = Integer.parseInt(str);
			}
		}
		return ary;
	}
/*----------------------主函数--------------------*/	
	public static void main(String[] args) {
		new ganttChart().setVisible(true);
	}
/*----------------------------------------------*/
	class CanvasPanel extends Canvas {
		public void paint(Graphics g) {
			int j = 0, m = 0;
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Shape backGround;
			backGround = new Rectangle2D.Double(0, 0, XMax, YMax);
			g2.setColor(Color.WHITE);
			g2.fill(backGround);
			// 画y轴
			int Y1 = Ystart - interval;// y轴 第一个点的y坐标
			int Y2 = Ystart + macNum * (unitHeight + interval) + interval;// y轴第二个点的y坐标
			g2.setColor(Color.BLACK);
			g2.drawLine(Xstart, Y1, Xstart, Y2);
			// 画x轴
			g2.drawLine(Xstart, Y2, (int) (Xstart + makespan * xproportion), Y2);
			// 画机器矩形框及M1 M2...
			for (m = 0; m < macNum; m++) {
				g2.setColor(BACKGROUND);
				g2.fill(rectMachine[m]);
				g2.setColor(Color.BLACK);
				g2.draw(rectMachine[m]);
				TextUtilities.paintString(g2, machineName[m], rectMachine[m].getBounds(), TextUtilities.CENTER,
						TextUtilities.CENTER);
			}

			int size = 0, jobIndexTmp;
			for (j = 0; j < macNum; j++) {
				size = begin.get(j).size();
				if (size == 0)
					continue;
				for (m = 0; m < size; m++) {
					jobIndexTmp = jobIndex.get(j).get(m);
					g2.setColor(col[jobIndexTmp]);
					g2.fill(rectProcess.get(j).get(m));
					g2.setColor(Color.BLACK);
					g2.draw(rectProcess.get(j).get(m));
					g2.setFont(font);

					// 关键工序标注
					if (guanjiangx.get(j).get(m) == 1) {
						g2.setColor(Color.red);
					}

					TextUtilities.paintString(g2, (jobIndexTmp + 1) + "", rectProcess.get(j).get(m).getBounds(),
							TextUtilities.CENTER, TextUtilities.CENTER);
					g2.setColor(Color.black);

					// 标注完工时间
					g2.setColor(Color.black);
					g2.setFont(font2);

					TextUtilities.paintString(g2, end.get(j).get(m) + "", rectStrFt.get(j).get(m).getBounds(),
							TextUtilities.CENTER, TextUtilities.CENTER);

//					// 标注开始时间
//					if (begin.get(j).get(m) != 0)
//						TextUtilities.paintString(g2, begin.get(j).get(m) + "", rectStrSt.get(j).get(m).getBounds(),
//								TextUtilities.CENTER, TextUtilities.CENTER);
				}
			}

		}
	}

	public static void drawSelPolygon(Graphics2D g2, Color frameColor, Shape a) {
		g2.setColor(frameColor);
		// 初始化多边形
		// 取得多边形外接矩形
		Rectangle r = a.getBounds();
		Rectangle2D r2 = new Rectangle2D.Double(r.x, r.y, r.width, r.height + 0.1);
		// 裁切
		g2.setClip(r2);
		// 绘制填充线
		for (int j = r.y; j < r.y + r.height + r.width; j = j + 8) {
			// <--NG
			Line2D line = new Line2D.Float(r.x, j, (r.x + r.width), j - r.width);
			g2.draw(line);
		}
		// 绘制填充线
		for (int j = r.y + r.height; j > r.y - r.height - r.width; j = j - 8) {
			// <--NG
			Line2D line = new Line2D.Float(r.x, j, (r.x + r.width), j + r.width);
			g2.draw(line);
		}
		// 绘制多边形
		g2.draw(a);
	}

	protected void do_this_windowOpened(WindowEvent e) {
		// 设置窗口相对于指定组件的位置 null时在屏幕中间
		setLocationRelativeTo(null);//
	}

}
