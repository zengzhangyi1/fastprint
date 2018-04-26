package cn.pojo;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="task")
public class Task {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	int taskId;
	int resuorceId;
	LocalDateTime startTime;
	LocalDateTime endTime;
	
	@ManyToOne
	@JoinColumn(name = "wid")
	Wop wop;
	
	@ManyToOne
	@JoinColumn(name="oid")
	Order order;
	
	@ManyToOne
	@JoinColumn(name="pid")
	Process process;
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getResuorceId() {
		return resuorceId;
	}

	public void setResuorceId(int resuorceId) {
		this.resuorceId = resuorceId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Wop getWop() {
		return wop;
	}

	public void setWop(Wop wop) {
		this.wop = wop;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}
	
	
}
