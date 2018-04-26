package cn.pojo;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="wop")
public class Wop {
	@Id
	@Column(name= "wopId")
	private String wopId;
	
	private String wopName;
	private int qty;
	private double width;
	private double processTime;
	
	@ManyToOne
	@JoinColumn(name="oid")
	Order order;
	
	@OneToOne
	@JoinColumn(name = "pid")
	Process process;
	
	@Transient
	LocalDateTime earlistStartTime;
	@Transient
	LocalDateTime latestEndTime;

	public String getWopId() {
		return wopId;
	}

	public void setWopId(String wopId) {
		this.wopId = wopId;
	}

	public String getWopName() {
		return wopName;
	}

	public void setWopName(String wopName) {
		this.wopName = wopName;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getProcessTime() {
		return processTime;
	}

	public void setProcessTime(double processTime) {
		this.processTime = processTime;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}
	
	public LocalDateTime getEarlistStartTime() {
		return earlistStartTime;
	}

	public void setEarlistStartTime(LocalDateTime earlistStartTime) {
		this.earlistStartTime = earlistStartTime;
	}

	public LocalDateTime getLatestEndTime() {
		return latestEndTime;
	}

	public void setLatestEndTime(LocalDateTime latestEndTime) {
		this.latestEndTime = latestEndTime;
	}

	@Override
	public String toString() {
		return "Wop [wopId=" + wopId + ", wopName=" + wopName + ", qty=" + qty + ", width=" + width + ", processTime="
				+ processTime + ", order=" + order.getOrderId() + ", process=" + process.getProcessName() + ", earlistStartTime=" 
				+ earlistStartTime 	+ ", latestEndTime=" + latestEndTime + "]";
	}

}
