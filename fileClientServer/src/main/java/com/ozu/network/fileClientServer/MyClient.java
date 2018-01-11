package com.ozu.network.fileClientServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.ozu.network.model.DownloadedBytePackage;
import com.ozu.network.model.FileDataResponseType;
import com.ozu.network.model.FileListResponseType;
import com.ozu.network.model.RequestType;
import com.ozu.network.model.ResponseType;


@SuppressWarnings("Since15")
public class MyClient extends Thread
{
	protected Thread runningThread = null;
	private Logger logger =loggerManager.getInstance(this.getClass());
	
    private Integer clientId;
    private String clientName;
    private Inet4Address ipAddress;
    private int portNumber;
    private  ServerInfo server;
    private int fileId;
    
    private int timeoutCount=0;
    


	private ByteCollector collector;

    public MyClient(Integer clientId, String clientName, Inet4Address ipAddress, int portNumber, ServerInfo server , ByteCollector collector)
    {
        this.setClientId(clientId);
        this.clientName = clientName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.server = server;
        this.collector= collector;
    }

    public String getInfo()
    {
        String info = "";
        if (this.clientId != null)
        {
            info += "Id: " + this.clientId.toString();
        }
        if (this.clientName != null)
        {
            info += " Ag: " + this.clientName;
        }
        if (this.ipAddress != null)
        {
            info += " IP: " + this.ipAddress.getHostAddress();
        }
        if (this.portNumber != 0)
        {
            info += " Port: " + this.portNumber;
        }
        return info;
    }

	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		while (this.isAlive()) {

			DownloadedBytePackage pack = collector.getNextPackage();
			if(pack != null)
			{
				try {
					FileDataResponseType dataResponse = getFileData(pack.getStartByteNumber(),pack.getEndByteNumber());
					if(dataResponse != null)
					{
						pack.setData(dataResponse.getData());
						pack.setCompleted(true);	
					}
					else
					{
						pack.setCompleted(false);
					}
					timeoutCount =0;
					
				}
				catch (SocketTimeoutException ex)
				{
					pack.setCompleted(false);
					pack.setTimeoutOccured(true);
					timeoutCount++;
				}
				catch (Exception e) {
					pack.setCompleted(false);
				}
				finally
				{
					collector.addDownloadedBytePackage(pack);
					if(timeoutCount >=2)
					{
						try {
							//logger.debug("Client is suspended because of timeout series  ::  " +this.getInfo()  + "  ::  server info   :: " + this.server.getInfo());
							this.runningThread.sleep(1500);
							timeoutCount=0;
						} catch (InterruptedException e) {
							//logger.error("Error Occured when Client is suspended because of timeout series  ::  " +this.getInfo()+ "  ::  server info   :: " + this.server.getInfo());
						}
					}
				}
				
			}
			else
			{
				if(this.collector.isCompleted())
				{
					logger.debug("Client work is stoped client Info ::  "  +getInfo());
					collector.removeClient(this.clientId);
					this.runningThread.stop();
				}
			}
		}

	}
	
	private FileDataResponseType getFileData(long start, long end) throws SocketException, SocketTimeoutException, IOException {

		InetAddress IPAddress = InetAddress.getByName(this.server.getIp());
		RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, this.fileId, start, end, null);
		byte[] sendData = req.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, this.server.getPortNumber());
		DatagramSocket dsocket = new DatagramSocket();
		dsocket.setSoTimeout(2000);
		dsocket.send(sendPacket);
		int sizeOfArray = (int)(end-start);
		byte[] receiveData = new byte[sizeOfArray+100];
		long maxReceivedByte = -1;
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		dsocket.receive(receivePacket);
		FileDataResponseType response = new FileDataResponseType(receivePacket.getData());
		//loggerManager.getInstance(this.getClass()).debug(response.toString());
		if (response.getResponseType() != ResponseType.RESPONSE_TYPES.GET_FILE_DATA_SUCCESS) {
			return null;
		}
		else
		{
			return response;
		}

	}
	
	


    public String getClientName()
    {
        return clientName;
    }

    public void setClientName(String clientName)
    {
        this.clientName = clientName;
    }

    public Inet4Address getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public void setPortNumber(int portNumber)
    {
        this.portNumber = portNumber;
    }

    public ServerInfo getServer()
    {
        return server;
    }

    public void setServer(ServerInfo server)
    {
        this.server = server;
    }

	public ByteCollector getCollector() {
		return collector;
	}

	public void setCollector(ByteCollector collector) {
		this.collector = collector;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}
	
    public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

}

