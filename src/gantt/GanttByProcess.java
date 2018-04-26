package gantt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import cn.pojo.Job;
import cn.pojo.Process;
import cn.pojo.Task;
import cn.service.Service;
import cn.utils.HibernateUtils;
import cn.utils.IOUtil;
import cn.utils.ResourceUtils;

/*根据工序号得到甘特图*/
public class GanttByProcess {
	Service service = Service.getService();
	
	public static void main(String[] args) {
		GanttByProcess gby = new GanttByProcess();
		gby.ganttByProcessId(20);
		HibernateUtils.closeAll();
	}
	
	public void ganttByProcessId(int pid) {
		Process process = (Process) service.get(Process.class, pid);
		
		if(process == null||pid==0) {
			System.out.println("工序号不存在!");
			return;
		}
		
		if(pid==10) {
			ganttBottleneck();
			return ;
		}
		@SuppressWarnings("unchecked")
		List<Task> tasks = service.list(Task.class,"process",process);
		
		if(tasks.isEmpty()) {
			System.out.println("没有工作任务！");
			return;
		}
		
		LocalDateTime EST = LocalDateTime.of(2018, 12, 31, 12, 0);
		LocalDateTime LET = LocalDateTime.of(2016, 1, 1, 12, 0);
		for (Task task : tasks) {
			if(task.getStartTime().isBefore(EST)) EST = task.getStartTime();
			if(task.getEndTime().isAfter(LET)) LET = task.getEndTime();
		}
		
		float makespan = EST.until(LET, ChronoUnit.MINUTES);
		IOUtil.writeTxtByTask(tasks);
		GanttChart gant = new GanttChart(process.getMainResource(),makespan, tasks.size());
		gant.setVisible(true);
	}
	
	public void ganttBottleneck() {
		@SuppressWarnings("unchecked")
		List<Job> jobs = service.list(Job.class);
		IOUtil.writeTxt(jobs);
		double makespan = 0;
		for (Job job : jobs) {
			makespan = Math.max(job.getEndTime(), makespan);
		}
		
		GanttChart gant = new GanttChart(ResourceUtils.getTankNum(),(float) makespan, jobs.size());
		gant.setVisible(true);
	}
}
