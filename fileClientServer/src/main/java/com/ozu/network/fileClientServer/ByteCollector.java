package com.ozu.network.fileClientServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ozu.network.model.CollectedBytePackageModel;
import com.ozu.network.model.DownloadedBytePackage;




public class ByteCollector extends Thread
{
	
	private Logger logger =loggerManager.getInstance(this.getClass());
	
	protected Thread runningThread = null;

	private MainByteCollector mainCollector;
	private List<DownloadedBytePackage> downloadedBytes;
	private List<DownloadedBytePackage> downloadList;
	private List<DownloadedBytePackage> notDownloadList;
	
	private HashMap<Integer, DownloadedBytePackage> processedBytelist;
	private HashMap<Integer, Integer> addedBytePackageCodes;
	
	private List<Integer> clients;
	private boolean timeOutOccured=false;
	
	private long startTime=0L;
	private long endTime=0L;
	
	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	private int nextPacketStartPoint;
	
	private int nextPackageSize=100;
	
	private int nextPackageId=1;
	private int downloadedByteCount=0;
	
	public List<DownloadedBytePackage> getNotDownloadList() {
		return notDownloadList;
	}


	public void setNotDownloadList(List<DownloadedBytePackage> notDownloadList) {
		this.notDownloadList = notDownloadList;
	}


	public int getDownloadedByteCount() {
		return downloadedByteCount;
	}


	public void setDownloadedByteCount(int downloadedByteCount) {
		this.downloadedByteCount = downloadedByteCount;
	}



	
	
	
	private int startByte;
	private int endByte;
	private int collectorId;
	
	
	
	private boolean completed=false;

	private boolean completedGenerated=false;

	private String fileName;

	private boolean finished =false;
	
	public ByteCollector()
	{
		this.downloadList = new ArrayList<DownloadedBytePackage>();
		this.downloadedBytes = new ArrayList<DownloadedBytePackage>();
		this.notDownloadList= new ArrayList<DownloadedBytePackage>();
		this.processedBytelist= new HashMap<Integer, DownloadedBytePackage>();
		this.clients = new ArrayList<Integer>();
		this.addedBytePackageCodes= new HashMap<Integer, Integer>();
	}


	public List<DownloadedBytePackage> getDownloadList() {
		return downloadList;
	}



	public void setDownloadList(List<DownloadedBytePackage> downloadList) {
		this.downloadList = downloadList;
	}



	public boolean isTimeOutOccured() {
		return timeOutOccured;
	}



	public void setTimeOutOccured(boolean timeOutOccured) {
		this.timeOutOccured = timeOutOccured;
	}



	public int getNextPacketStartPoint() {
		return nextPacketStartPoint;
	}



	public void setNextPacketStartPoint(int nextPacketStartPoint) {
		this.nextPacketStartPoint = nextPacketStartPoint;
	}



	public int getNextPackageSize() {
		return nextPackageSize;
	}



	public void setNextPackageSize(int nextPackageSize) {
		this.nextPackageSize = nextPackageSize;
	}



	public int getNextPackageId() {
		return nextPackageId;
	}



	public void setNextPackageId(int nextPackageId) {
		this.nextPackageId = nextPackageId;
	}






	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		while (this.isAlive() && !this.finished) {

			if (this.completed && this.clients != null && this.clients.size() == 0) {




				CollectedBytePackageModel collectedPackage = new CollectedBytePackageModel();

				collectedPackage.setPackageId(this.collectorId);
				collectedPackage.setStartByteNumber(this.startByte);
				collectedPackage.setEndByteNumber(this.endByte);
				

				this.downloadedBytes.sort(new Comparator<DownloadedBytePackage>() {

					@Override
					public int compare(DownloadedBytePackage arg0, DownloadedBytePackage arg1) {
						return Integer.compare(arg0.getStartByteNumber(), arg1.getStartByteNumber());
					}
				});

				List<Byte> dataList = new ArrayList<Byte>();
				for (DownloadedBytePackage pack : this.downloadedBytes) {

					for (Byte b : pack.getData()) {
						dataList.add(b);
					}
				}

				byte[] data = new byte[dataList.size()];

				int counter = 0;
				for (byte b : dataList) {
					data[counter] = b;
					counter++;
				}

				collectedPackage.setData(data);
				
				logger.debug("Downloaded byte size ::  " + dataList.size() + "  --  Must downloaded Byte size   " + (this.endByte - this.startByte + 1));
				collectedPackage.setPackageSize(dataList.size());
				

				mainCollector.getPackages().add(collectedPackage);

				this.finished = true;
			} else {
				if (this.downloadedByteCount >= (this.endByte - this.startByte + 1)) {

					endTime = new Date().getTime();
					this.completed = true;
				}
			}
		}
		if (this.finished) {
			logger.debug("ByteCollector " + this.collectorId + " work is stoped");
			mainCollector.removeClient(this.collectorId);
			this.runningThread.stop();
		}

	}
	
	
	public synchronized void removeClient(Integer clientId)
	{
		clients.remove(clientId);
	}




	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public List<DownloadedBytePackage> getDownloadedBytes() {
		return downloadedBytes;
	}



	public void setDownloadedBytes(List<DownloadedBytePackage> downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}

	
	public synchronized DownloadedBytePackage getNextPackage() {

		DownloadedBytePackage pack = null;
		if (this.completedGenerated && downloadList != null && downloadList.size() == 0){
			if(this.notDownloadList != null && notDownloadList.size()>0)
			{
				pack = notDownloadList.get(0);
				notDownloadList.remove(0);
				processedBytelist.put(pack.getPackageId(), pack);
				return pack;
			}
			else
			{
				if (processedBytelist.values() != null) {
					for (DownloadedBytePackage p : processedBytelist.values()) {
							notDownloadList.add(p);
					}
					processedBytelist = new HashMap<Integer, DownloadedBytePackage>();
				}
				
				if(notDownloadList != null  && notDownloadList.size()>0)
				{
					pack = notDownloadList.get(0);
					notDownloadList.remove(0);
					processedBytelist.put(pack.getPackageId(), pack);
					return pack;
				}
				else
				{
					return null;
				}
			}
		} else {
			
			if (downloadList != null && downloadList.size() > 0) {
				pack = downloadList.get(0);
				downloadList.remove(0);
				processedBytelist.put(pack.getPackageId(), pack);
			}

			if (downloadList != null &&downloadList.size() < 4) {
				DownloadedBytePackage nextPack = generateNextPackage();
				if (nextPack != null) {
					downloadList.add(nextPack);
				}
			}

			return pack;
		}
	}
	
	private synchronized DownloadedBytePackage generateNextPackage() {
		if(startTime == 0)
		{
			startTime =new Date().getTime();
		}
		if(this.nextPacketStartPoint >= this.endByte)
		{
//			logger.debug("Last byte downloaded" + new Date().getTime());
			
			this.setCompletedGenerated(true);
			

			return null;
		}
		
		if(timeOutOccured)
		{
			logger.debug("Timeout Occured" + new Date().getTime());
			this.nextPackageSize=100;
			this.timeOutOccured=false;
		}
		else
		{
			if(this.nextPackageSize <=600)
			this.nextPackageSize = this.nextPackageSize +100;
			
		}

		if(this.endByte -this.nextPacketStartPoint < this.nextPackageSize)
		{
			DownloadedBytePackage nextPack = new DownloadedBytePackage();
			nextPack.setCompleted(false);
			nextPack.setPackageId(this.nextPackageId);
			nextPack.setStartByteNumber(this.nextPacketStartPoint);
			nextPack.setEndByteNumber(this.endByte);
			this.nextPackageId++;
			this.nextPacketStartPoint = nextPack.getEndByteNumber() +1;
			
			//logger.debug("Pack number " + nextPack.getPackageId() + " created   " + new Date().getTime() + "last generated byte number is " + nextPack.getEndByteNumber());
			return nextPack;
		}
		else
		{
			DownloadedBytePackage nextPack = new DownloadedBytePackage();
			nextPack.setCompleted(false);
			nextPack.setPackageId(this.nextPackageId);
			nextPack.setStartByteNumber(this.nextPacketStartPoint);
			nextPack.setEndByteNumber(this.nextPacketStartPoint + this.nextPackageSize);
			this.nextPackageId++;
			this.nextPacketStartPoint = nextPack.getEndByteNumber() +1;
			//logger.debug("Pack number " + nextPack.getPackageId() + " created   " + new Date().getTime() + "last generated byte number is " + nextPack.getEndByteNumber());
			return nextPack;
		}
	}



	public synchronized void addDownloadedBytePackage(DownloadedBytePackage pack)
	{
	
		if(pack.isCompleted())
		{
			if (!this.addedBytePackageCodes.containsKey(pack.getPackageId())) {
				this.downloadedBytes.add(pack);
				downloadedByteCount += pack.getData().length;
				//logger.debug("Pack number " + pack.getPackageId() + " downloaded successfully   " + new Date().getTime() + "   downloaded byte code = "						+ downloadedByteCount);
				this.addedBytePackageCodes.put(pack.getPackageId(), pack.getPackageId());
			}

		}
		else
		{
			if(pack.isTimeoutOccured())
			{
				this.timeOutOccured =true;
				pack.setTimeoutOccured(false);
			}
			this.notDownloadList.add(pack);
//			logger.debug("Pack number " + pack.getPackageId() + " not downloaded and added to queue   " + new Date().getTime());
		}
		
		this.processedBytelist.remove(pack.getPackageId());
	}


	public boolean isCompleted() {
		return completed;
	}


	public void setCompleted(boolean completed) {
		this.completed = completed;
	}


	public boolean isCompletedGenerated() {
		return completedGenerated;
	}


	public void setCompletedGenerated(boolean completedGenerated) {
		this.completedGenerated = completedGenerated;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	public HashMap<Integer, DownloadedBytePackage> getProcessedBytelist() {
		return processedBytelist;
	}


	public void setProcessedBytelist(HashMap<Integer, DownloadedBytePackage> processedBytelist) {
		this.processedBytelist = processedBytelist;
	}


	public List<Integer> getClients() {
		return clients;
	}


	public void setClients(List<Integer> clients) {
		this.clients = clients;
	}


	public MainByteCollector getMainCollector() {
		return mainCollector;
	}


	public void setMainCollector(MainByteCollector mainCollector) {
		this.mainCollector = mainCollector;
	}


	public int getCollectorId() {
		return collectorId;
	}


	public void setCollectorId(int collectorId) {
		this.collectorId = collectorId;
	}


	public int getEndByte() {
		return endByte;
	}


	public void setEndByte(int endByte) {
		this.endByte = endByte;
	}


	public int getStartByte() {
		return startByte;
	}


	public void setStartByte(int startByte) {
		this.startByte = startByte;
		this.nextPacketStartPoint =startByte;
	}









	
}

