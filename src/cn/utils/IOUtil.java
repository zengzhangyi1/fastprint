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
                // 创建文件字符流
                FileWriter fw = new FileWriter(file);
                // 缓存流 
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
		//找到最早开始时间
		LocalDateTime EST = LocalDateTime.of(2018, 12, 31, 12, 0);
		for (Task task : tasks) {
			if(task.getStartTime().isBefore(EST)) EST = task.getStartTime();
		}
		try (
                // 创建文件字符流
                FileWriter fw = new FileWriter(file);
                // 缓存流 
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
	
	//根据excel里面时间分布的比例计算
	public static void  readExcel(List<Job> jobs) {
		jxl.Workbook readwb = null;   
		try 
        {   
            //构建Workbook对象, 只读Workbook对象   
            //直接从本地文件创建Workbook   
            InputStream instream = new FileInputStream("data/input.xls");   
            readwb = Workbook.getWorkbook(instream);   
            
            //Sheet的下标是从0开始
            //获取第一张Sheet表
            Sheet readsheet = readwb.getSheet(0); 
            
            //获取Sheet表中所包含的总列数   
            int rsRows = readsheet.getRows(); 
            //获取指定单元格的对象引用  
            
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
