package cn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import cn.pojo.Job;
import cn.pojo.Task;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class IOUtil {
	public static void writeTxt(List<Job> jobs) {
		File file = new File("data/tongyong.txt");
		try (
                // �����ļ��ַ���
                FileWriter fw = new FileWriter(file);
                // ������ 
                PrintWriter pw = new PrintWriter(fw);              
        ) {
			for(int i=0;i<jobs.size();i++){
				Job job  = jobs.get(i);
				pw.print(job.getJobId()+"	");
				pw.print(1+"	");
				pw.print(job.getStartTime().intValue()+"	");
				pw.print(job.getEndTime().intValue()+"	");
				pw.print(job.getResourceId()+"	");
				pw.println(0);
			}
			
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void writeTxtByTask(List<Task> tasks) {
		File file = new File("data/tongyong.txt");
		//�ҵ����翪ʼʱ��
		LocalDateTime EST = LocalDateTime.of(2018, 12, 31, 12, 0);
		for (Task task : tasks) {
			if(task.getStartTime().isBefore(EST)) EST = task.getStartTime();
		}
		try (
                // �����ļ��ַ���
                FileWriter fw = new FileWriter(file);
                // ������ 
                PrintWriter pw = new PrintWriter(fw);              
        ) {
			for(int i=0;i<tasks.size();i++){
				Task task  = tasks.get(i);
				pw.print(i+1+"	");
				pw.print(1+"	");
				pw.print(EST.until(task.getStartTime(), ChronoUnit.MINUTES)+"	");
				pw.print(EST.until(task.getEndTime(), ChronoUnit.MINUTES)+"	");
				pw.print(task.getResuorceId()+"	");
				pw.println(0);
			}
			
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	//����excel����ʱ��ֲ��ı�������
	public static void  readExcel(List<Job> jobs) {
		jxl.Workbook readwb = null;   
		try 
        {   
            //����Workbook����, ֻ��Workbook����   
            //ֱ�Ӵӱ����ļ�����Workbook   
            InputStream instream = new FileInputStream("data/input.xls");   
            readwb = Workbook.getWorkbook(instream);   
            
            //Sheet���±��Ǵ�0��ʼ
            //��ȡ��һ��Sheet��
            Sheet readsheet = readwb.getSheet(0); 
            
            //��ȡSheet������������������   
            int rsRows = readsheet.getRows(); 
            //��ȡָ����Ԫ��Ķ�������  
            
            int jobId = 1;
            
            for (int i = 1; i < rsRows; i++) 
            {
            	Cell c2 = readsheet.getCell(2, i);
            	int count = Integer.parseInt(c2.getContents());
            	
            	for(int j = 0;j<count;j++) {
            		Job job = new Job();
            		
            		Cell c1 = readsheet.getCell(1, i);
            		double processTime = Double.parseDouble(c1.getContents());
            		job.setProcessTime(processTime);
            		job.setJobId(jobId++);
            		jobs.add(job);
            	}
            }
        }catch (Exception e) 
        	{e.printStackTrace();} 
		finally 
			{readwb.close();}
	}
}
