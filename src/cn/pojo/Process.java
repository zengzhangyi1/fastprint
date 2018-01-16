package cn.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="process")
public class Process {
	@Id
	@Column(name="processId")
	int processId;
	String processName;
	int mainResource;
	int subResource;
	public int getProcessId() {
		return processId;
	}
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public int getMainResource() {
		return mainResource;
	}
	public void setMainResource(int mainResource) {
		this.mainResource = mainResource;
	}
	public int getSubResource() {
		return subResource;
	}
	public void setSubResource(int subResource) {
		this.subResource = subResource;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Process other = (Process)obj;
		if(this.getProcessId()==other.getProcessId()&&this.getProcessName().equals(other.getProcessName()))
			return true;
		else return false;
	}
	
	
}
