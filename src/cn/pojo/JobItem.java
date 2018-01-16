package cn.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name= "jobitem")
public class JobItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	int jobItemId;
	
	int qty;
	boolean isfull;
	
	@ManyToOne
	@JoinColumn(name="oid")
	Order order;
	
	@ManyToOne
	@JoinColumn(name = "wid")
	Wop wop;
	
	@ManyToOne
	@JoinColumn(name="jid")
	Job job;
	

	@Override
	public String toString() {
		return "JobItem [jobItemId=" + jobItemId + ", qty=" + qty + ", isfull=" + isfull + "£¬length =" +wop.getWidth()*qty+
				", order=" + order.getOrderId() + ", wop=" + wop.getWopId() + ", job=" + job + "]";
	}

	public int getJobItemId() {
		return jobItemId;
	}

	public void setJobItemId(int jobItemId) {
		this.jobItemId = jobItemId;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
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

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public boolean isIsfull() {
		return isfull;
	}

	public void setIsfull(boolean isfull) {
		this.isfull = isfull;
	}
	
}
