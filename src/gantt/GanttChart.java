/**
 * 
 */
package gantt;
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

@SuppressWarnings("serial")
public class GanttChart extends JFrame {
	
	private int macNum = 10;// ������
	private int makespan = 100;
	private int rows = 2;// ��ȡ���ݵ�ʱ���õ���test.txt�����ݵ�����
	private Map<Integer, List<Integer>> begin = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> end = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> jobIndex = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> gongxuIndex = new HashMap<>(macNum);
	private Map<Integer, List<Integer>> guanjiangx = new HashMap<>(macNum);

	private int unitHeight = 20;
	private int macLen = 30;// ��������ĳ���
	private int interval = 20;
	private float xproportion = 2.5f;
	private float XMax; //
	private int YMax;
	private int Ystart = 12; // Ystart Xstart ����ΪM1���ο�����Ͻ�����
	private int Xstart = 35;

	private Shape[] rectMachine;// y����ϵ� ��������
	private Map<Integer, List<Shape>> rectProcess = new HashMap<>(macNum);
	private Map<Integer, List<Shape>> rectStrFt = new HashMap<>(macNum);// ������ע�깤ʱ��ľ��ο�
	private Map<Integer, List<Shape>> rectStrSt = new HashMap<>(macNum);// ������ע��ʼ�ӹ�ʱ��ľ��ο�
	private Map<Integer, List<Shape>> rectSetup = new HashMap<>(macNum);// ׼��ʱ��ľ��ο�
	Font font; // rectProcess���ο��ϵ�����
	Font font2;// rectStrFt���ο��ϵ�����

	private String machineName[];

	private static final Random rand = new Random(); // �����������
	public static final Color BACKGROUND = new Color(180, 180, 255);
	public static final Color PURPLE = new Color(249, 204, 226); // ��ɫ/
	public static final Color DeepSkyBlue = new Color(250, 250, 250); // ����/
	public static final Color Auqamarin = new Color(0, 255, 127); // ����ɫ/
	public static final Color GreenYellow = new Color(173, 255, 47); // �̻�ɫ/
	public static final Color Olive = new Color(128, 128, 0); // ���ɫ/
	public static final Color Coral = new Color(255, 222, 173); // ɺ��ɫ/
	public static final Color LightSkyBlue = new Color(135, 206, 250); // ����ɫ
	Color[] col0 = new Color[] { Auqamarin, PURPLE, GreenYellow, Color.MAGENTA, Color.GREEN, Color.YELLOW, Color.GRAY,
			Color.PINK, Coral, Olive, Color.CYAN };
	Color[] col;

	public GanttChart(int macNum,float makespan,int rows) {
		this.macNum = macNum;
		this.makespan = (int)(makespan+100);
		this.rows = rows;
		this.xproportion = 1250/makespan;
		col = new Color[rows];
		try {
			int i = 0, s = 0;
			for (i = 0; i < col0.length; i++) {
				col[i] = col0[i];
			}
			int r;
			int g;
			int b;
			for (i = col0.length; i < rows; i++) {
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

					// ���涨�� ������ע�깤ʱ��ľ��ο�
					x1 = Xstart + endTmp * xproportion;
					y1 = Ystart + (i + 1) * unitHeight + i * interval;
					len = 30;
					rectStrFtList.add(new Rectangle2D.Double(x1 - len / 2, y1, len, interval));

					// ���涨�� ������ע��ʼ�ӹ�ʱ��ľ��ο�
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
			this.setTitle("����ͼ");
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					do_this_windowOpened(e);
				}
			});
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			font = new Font("����", Font.BOLD, 12);
			font2 = new Font("����", Font.BOLD, 11);
			add(new CanvasPanel());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void mapMacI_StageI(int stageNum, HashMap<Integer, Integer> map, Integer[] mac) {

		int count; // �洢�ý׶�֮ǰ���н׶��ܹ��Ļ�����
		for (int s = 0; s < stageNum; s++) {
			count = 0;
			for (int c = 0; c < s; c++)
				count += mac[c];
			for (int m = count; m < count + mac[s]; m++)
				map.put(m, s);
		}

	}
// <2>��ȡ���� ��ʽ test������0 �����1 ��ʼʱ��2 ����ʱ��3 ������4 �Ƿ��ǹؼ�����5
	public void readData2() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("data/tongyong.txt"));
		Integer[][] arr = new Integer[rows][6];
		int index = 0;
		String temp;
		while ((temp = br.readLine()) != null) {
			arr[index++] = aryChange(temp);
		}
		br.close();

		// ��ʼ��
		for (int i = 0; i < macNum; i++) {
			begin.put(i, new ArrayList<Integer>());
			end.put(i, new ArrayList<Integer>());
			jobIndex.put(i, new ArrayList<Integer>());
			gongxuIndex.put(i, new ArrayList<Integer>());
			guanjiangx.put(i, new ArrayList<Integer>());
		}

		// ������0 �����1 ��ʼʱ��2 ����ʱ��3 ������4 �Ƿ��ǹؼ�����5
		// ��test.txt�й��������򣬻�����ž���1/0��ʼ��
		int macTmp;
		for (int i = 0; i < rows; i++) {
			
//			/*---------����������������Ŵ�0��ʼ--------------------*/
//			macTmp = arr[i][4];
//			jobIndex.get(macTmp).add(arr[i][0]);
//			gongxuIndex.get(macTmp).add(arr[i][1]);
			
			/*---------����������������Ŵ�1��ʼ--------------------*/
			macTmp = arr[i][4]-1;
			jobIndex.get(macTmp).add(arr[i][0]-1);
			gongxuIndex.get(macTmp).add(arr[i][1]-1);

			begin.get(macTmp).add(arr[i][2]);
			end.get(macTmp).add(arr[i][3]);
			guanjiangx.get(macTmp).add(arr[i][5]);
		}
	}
	// ������ʽ��\sƥ���κοհ��ַ��������ո��Ʊ������ҳ�� + ��ʾһ������
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

	class CanvasPanel extends Canvas {
		public void paint(Graphics g) {
			int j = 0, m = 0;
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			Shape backGround;
			backGround = new Rectangle2D.Double(0, 0, XMax, YMax);
			g2.setColor(Color.WHITE);
			g2.fill(backGround);
			// ��y��
			int Y1 = Ystart - interval;// y�� ��һ�����y����
			int Y2 = Ystart + macNum * (unitHeight + interval) + interval;// y��ڶ������y����
			g2.setColor(Color.BLACK);
			g2.drawLine(Xstart, Y1, Xstart, Y2);
			// ��x��
			g2.drawLine(Xstart, Y2, (int) (Xstart + makespan * xproportion), Y2);
			// ���������ο�M1 M2...
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

					// �ؼ������ע
					if (guanjiangx.get(j).get(m) == 1) {
						g2.setColor(Color.red);
					}

					TextUtilities.paintString(g2, (jobIndexTmp + 1) + "", rectProcess.get(j).get(m).getBounds(),
							TextUtilities.CENTER, TextUtilities.CENTER);
					g2.setColor(Color.black);

					// ��ע�깤ʱ��
					g2.setColor(Color.black);
					g2.setFont(font2);

					TextUtilities.paintString(g2, end.get(j).get(m) + "", rectStrFt.get(j).get(m).getBounds(),
							TextUtilities.CENTER, TextUtilities.CENTER);

//					// ��ע��ʼʱ��
//					if (begin.get(j).get(m) != 0)
//						TextUtilities.paintString(g2, begin.get(j).get(m) + "", rectStrSt.get(j).get(m).getBounds(),
//								TextUtilities.CENTER, TextUtilities.CENTER);
				}
			}

		}
	}

	public static void drawSelPolygon(Graphics2D g2, Color frameColor, Shape a) {
		g2.setColor(frameColor);
		// ��ʼ�������
		// ȡ�ö������Ӿ���
		Rectangle r = a.getBounds();
		Rectangle2D r2 = new Rectangle2D.Double(r.x, r.y, r.width, r.height + 0.1);
		// ����
		g2.setClip(r2);
		// ���������
		for (int j = r.y; j < r.y + r.height + r.width; j = j + 8) {
			// <--NG
			Line2D line = new Line2D.Float(r.x, j, (r.x + r.width), j - r.width);
			g2.draw(line);
		}
		// ���������
		for (int j = r.y + r.height; j > r.y - r.height - r.width; j = j - 8) {
			// <--NG
			Line2D line = new Line2D.Float(r.x, j, (r.x + r.width), j + r.width);
			g2.draw(line);
		}
		// ���ƶ����
		g2.draw(a);
	}

	protected void do_this_windowOpened(WindowEvent e) {
		// ���ô��������ָ�������λ�� nullʱ����Ļ�м�
		setLocationRelativeTo(null);//
	}

}
