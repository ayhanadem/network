package com.ozu.network.fileClientServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.ozu.network.model.FileDescriptor;
import com.ozu.network.model.FileListResponseType;
import com.ozu.network.model.FileSizeResponseType;
import com.ozu.network.model.InetAddressInterface;
import com.ozu.network.model.RequestType;

public class MyController implements Serializable {

	private Logger logger = loggerManager.getInstance(this.getClass());

	/**
	 * 
	 */
	private static final long serialVersionUID = -992417991356457863L;

	public static final String PROPERTIES_FILE = "conf/server.properties";

	public MyController() {

	}
	private List<InetAddressInterface> myInterfaces;
	private List<ServerInfo> myServersToReach;

	private FileListResponseType fileResponseType;
	private int fileId;
	private String fileName;
	private long selectedFileSize;

	private MainByteCollector mainCollector = new MainByteCollector();

	protected void prepareClientInterfaces() throws SocketException {
		myInterfaces = new ArrayList<InetAddressInterface>();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface net : Collections.list(nets)) {
			if (net.isUp()) {
				for (InetAddress inet_adr : Collections.list(net.getInetAddresses())) {
					if (inet_adr instanceof Inet4Address) {
						InetAddressInterface adr = new InetAddressInterface(net, inet_adr);
						myInterfaces.add(adr);
					}
				}
			}
		}

		if (!myInterfaces.isEmpty()) {
			for (int i = 0; i < myInterfaces.size(); i++) {
				InetAddressInterface adr = myInterfaces.get(i);
				System.out.println("[" + (i + 1) + "] " + adr.toString());
			}

		}
	}

	protected void prepareServerInfos() throws SocketException {

		myServersToReach = new ArrayList<ServerInfo>();
		String server1Address = "";
		String server2Address = "";

		Properties prop = new Properties();
		InputStream propFile = null;
		try {
			propFile = new FileInputStream(PROPERTIES_FILE);
			prop.load(propFile);
			server1Address = prop.getProperty("SERVER1");
			server2Address = prop.getProperty("SERVER2");
		} catch (IOException ex) {
			logger.debug(ex.toString());
		} catch (NumberFormatException ex) {
			logger.debug(ex.toString());
		} finally {
			if (propFile != null) {
				try {
					propFile.close();
				} catch (IOException e) {
					logger.error(e.toString());
				}
			}
		}

		int server_id = 1;
		if (server1Address != null && !server1Address.equals("")) {
			ServerInfo server = new ServerInfo(server_id, server1Address);
			myServersToReach.add(server);
			server_id++;
		}
		if (server2Address != null && !server2Address.equals("")) {
			ServerInfo server = new ServerInfo(server_id, server2Address);
			myServersToReach.add(server);
		}

		for (ServerInfo server : myServersToReach) {
			System.out.println(server.getInfo());
		}

	}

	private int serverId =0;
	protected FileListResponseType getFileList() throws IOException {

		try
		{
		
		ServerInfo server = myServersToReach.get(serverId);
		InetAddress IPAddress = InetAddress.getByName(server.getIp());
		RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_LIST, 0, 0, 0, null);
		byte[] sendData = req.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, server.getPortNumber());
		DatagramSocket dsocket = new DatagramSocket();
		dsocket.setSoTimeout(3000);
		dsocket.send(sendPacket);
		byte[] receiveData = new byte[720];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		dsocket.receive(receivePacket);
		FileListResponseType response = new FileListResponseType(receivePacket.getData());
		logger.debug(response.toString());
		return response;
	}
		catch (SocketTimeoutException ex)
		{
			if(serverId ==0)
			{
				serverId =1;
			}
			else
			{
				serverId =0;
			}
			logger.error(ex + " Server info ::: " + myServersToReach.get(serverId).getInfo());
			throw ex;
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	public List<InetAddressInterface> getMyInterfaces() {
		return myInterfaces;
	}

	public void setMyInterfaces(List<InetAddressInterface> myInterfaces) {
		this.myInterfaces = myInterfaces;
	}

	public List<ServerInfo> getMyServersToReach() {
		return myServersToReach;
	}

	public void setMyServersToReach(List<ServerInfo> myServersToReach) {
		this.myServersToReach = myServersToReach;
	}

	public void prepareFileListAndChooseFile() throws IOException {

		FileListResponseType fileList = getFileList();

		if (fileList.getFileDescriptors() != null && fileList.getFileDescriptors().length > 0) {
			HashMap<Integer, String> files = new HashMap<Integer, String>();
			for (FileDescriptor file : fileList.getFileDescriptors()) {

				System.out.println("File ID: " + file.getFile_id() + "  --  File Name :  " + file.getFile_name());
				files.put(file.getFile_id(), file.getFile_name());
			}
			fileId = -1;
			while (fileId == -1) {
				System.out.print("Please select file by entering the number in []:");
				Scanner in = new Scanner(System.in);
				try {
					Integer file_Id = Integer.valueOf(in.next()).intValue();
					if (files.containsKey(file_Id)) {

						this.fileId = file_Id;
						this.fileName = files.getOrDefault(fileId, "");
					} else {
						fileId = -1;
					}

				} catch (Exception ex) {
				}
			}

			System.out.println("Selected file is : [" + this.fileId + "]- " + this.fileName);

			System.out.println("File size : " + getFileSize(this.fileId));
		}
	}

	private long getFileSize(int file_id) throws IOException {
		ServerInfo server = myServersToReach.get(0);
		InetAddress IPAddress = InetAddress.getByName(server.getIp());
		RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_SIZE, file_id, 0, 0, null);
		byte[] sendData = req.toByteArray();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, server.getPortNumber());
		DatagramSocket dsocket = new DatagramSocket();
		dsocket.setSoTimeout(3000);
		dsocket.send(sendPacket);
		byte[] receiveData = new byte[1000];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		dsocket.receive(receivePacket);
		FileSizeResponseType response = new FileSizeResponseType(receivePacket.getData());
		loggerManager.getInstance(this.getClass()).debug(response.toString());
		this.selectedFileSize = response.getFileSize();
		return response.getFileSize();
	}

	public void prepareDownloadAndDownloadData() {

		int collectorCount=1;
		int partialFileSize = (int) this.selectedFileSize / collectorCount;

		this.mainCollector = new MainByteCollector();
		this.mainCollector.start();

		for (int c = 0; c < collectorCount; c++) {

			this.mainCollector.getByteCollectorClients().add(c + 1);
			ByteCollector collector = new ByteCollector();

			if (c < collectorCount) {

				collector.setEndByte((c + 1) * partialFileSize);
			} else {
				collector.setEndByte((int) this.selectedFileSize);
			}
			
			if(c==0)
			{
				collector.setStartByte(1);

			}
			else
			{
				collector.setStartByte((c * partialFileSize) );

			}
			collector.setMainCollector(this.mainCollector);
			collector.setCollectorId(c + 1);
			collector.setFileName(this.fileName);
			collector.start();
			int count = 1;
			for (int i = 0; i < 4; i++) {
				for (InetAddressInterface inf : myInterfaces) {

					for (ServerInfo server : myServersToReach) {
						MyClient client = new MyClient(count, inf.getNetqorkInterface().getDisplayName(), (Inet4Address) inf.getInetAddress(), 5000 +(c*32) +count,
								server, collector);
						client.setFileId(this.fileId);

						client.start();
						
						mainCollector.setStartTime(new Date().getTime());
						collector.getClients().add(count);
						count++;

					}
				}

			}

		}

	}

}
