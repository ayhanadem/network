package com.ozu.network.model;



public class CollectedBytePackageModel {
	
	
	
	private int packageId;
	private int startByteNumber;
	private int endByteNumber;
	private byte[] data;
	private int packageSize;

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
	public int getPackageSize() {
		return packageSize;
	}
	public void setPackageSize(int packageSize) {
		this.packageSize = packageSize;
	}
	
	
}
