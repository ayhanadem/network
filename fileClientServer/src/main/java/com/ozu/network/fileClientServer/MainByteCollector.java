package com.ozu.network.fileClientServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ozu.network.model.CollectedBytePackageModel;
import com.ozu.network.model.DownloadedBytePackage;




public class MainByteCollector extends Thread
{
	
	
	
	public MainByteCollector()
	{

		this.byteCollectorClients = new ArrayList<Integer>();
		this.packages= new ArrayList<CollectedBytePackageModel>();
		
	}
	private Logger logger =loggerManager.getInstance(this.getClass());
	
	protected Thread runningThread = null;

	private List<Integer> byteCollectorClients;
	
	private boolean timeOutOccured=false;
	
	private long startTime=0L;
	private long endTime=0L;
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private int downloadedByteCount=0;
	
	private List<CollectedBytePackageModel> packages;


	public int getDownloadedByteCount() {
		return downloadedByteCount;
	}

	public void setDownloadedByteCount(int downloadedByteCount) {
		this.downloadedByteCount = downloadedByteCount;
	}


	private int fileSize=0;
	private boolean completed=false;

	private boolean completedGenerated=false;

	private String fileName;

	private boolean finished =false;
	



	



	public boolean isTimeOutOccured() {
		return timeOutOccured;
	}



	public void setTimeOutOccured(boolean timeOutOccured) {
		this.timeOutOccured = timeOutOccured;
	}



	



	public int getFileSize() {
		return fileSize;
	}



	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}



	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		while (this.isAlive() && !this.finished ) {

			if(this.byteCollectorClients != null && this.byteCollectorClients.size()==0)
			{
				this.finished =true;
				this.endTime = new Date().getTime();
			}
		}
		
		if(this.finished)
		{
			createFile();
			prepareResultLog();
		}

	}
	
	
	
	
	public synchronized void removeClient(Integer clientId)
	{
		byteCollectorClients.remove(clientId);
	}
	synchronized void  createFile() {

		try {

			int totalSize=0;
			for (CollectedBytePackageModel pack : this.packages) {
				totalSize += pack.getPackageSize();
			}
			
			
			
			byte[] dataList = new byte[totalSize];
			
			for (CollectedBytePackageModel pack : this.packages) {

				for (int i = 0; i < pack.getData().length; i++) {
					dataList[pack.getStartByteNumber() - 1 +i] = pack.getData()[i];
				}
			}


			FileOutputStream out = null;
			try {
				out = new FileOutputStream("downloaded/" + this.fileName);
				out.write(dataList);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	synchronized void prepareResultLog()
    {
        long elapsedTime = this.getEndTime() - this.getStartTime();
        Util.logMessage("Toplam sure: " + elapsedTime + " ms.");
        long totalBytesDownloaded = this.getDownloadedByteCount();
        if (totalBytesDownloaded > 0)
        {
            Util.logMessage("Toplam indirilen byte: " + totalBytesDownloaded + " B.");

            double elapsedTimeAsSecond = ((double) elapsedTime) / 1000;
            double meanRate = (totalBytesDownloaded * 8) / elapsedTimeAsSecond;
            DecimalFormat df = new DecimalFormat("#0.0");
            Util.logMessage("Ortalama indirme hizi: " + df.format(meanRate) + " bps.");
        }
        Util.logMessage("md5: " + Util.getMd5(new File("./downloaded/" + this.fileName)));
    }



	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public List<Integer> getByteCollectorClients() {
		return byteCollectorClients;
	}


	public void setByteCollectorClients(List<Integer> byteCollectorClients) {
		this.byteCollectorClients = byteCollectorClients;
	}

	public List<CollectedBytePackageModel> getPackages() {
		return packages;
	}

	public void setPackages(List<CollectedBytePackageModel> packages) {
		this.packages = packages;
	}

	
	
	
}

