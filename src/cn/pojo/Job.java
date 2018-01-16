package cn.pojo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "job")
public class Job {
	@Id
	@Column(name="jobId")
	int jobId;
	
	Double processTime;
	Integer resourceId;
	Double startTime;
	Double endTime;
	
	@Transient
	List<JobItem> jobItems;
	
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public Double getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Double processTime) {
		this.processTime = processTime;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public Double getStartTime() {
		return startTime;
	}

	public void setStartTime(Double startTime) {
		this.startTime = startTime;
	}

	public Double getEndTime() {
		return endTime;
	}

	public void setEndTime(Double endTime) {
		this.endTime = endTime;
	}

	public List<JobItem> getJobItems() {
		return jobItems;
	}

	public void setJobItems(List<JobItem> jobItems) {
		this.jobItems = jobItems;
	}

}
