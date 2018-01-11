package com.ozu.network.model;



public class DownloadedBytePackage {
	
	
	
	private int packageId;
	private int startByteNumber;
	private int endByteNumber;
	private byte[] data;
	private boolean isCompleted=false;
	private boolean timeoutOccured=false;
	public int getPackageId() {
		return packageId;
	}
	public void setPackageId(int packageId) {
		this.packageId = packageId;
	}
	public int getStartByteNumber() {
		return startByteNumber;
	}
	public void setStartByteNumber(int startByteNumber) {
		this.startByteNumber = startByteNumber;
	}
	public int getEndByteNumber() {
		return endByteNumber;
	}
	public void setEndByteNumber(int endByteNumber) {
		this.endByteNumber = endByteNumber;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public boolean isCompleted() {
		return isCompleted;
	}
	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
	public boolean isTimeoutOccured() {
		return timeoutOccured;
	}
	public void setTimeoutOccured(boolean timeoutOccured) {
		this.timeoutOccured = timeoutOccured;
	}
	
	
}
