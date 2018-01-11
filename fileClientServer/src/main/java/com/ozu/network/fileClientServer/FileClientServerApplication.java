package com.ozu.network.fileClientServer;

import java.io.IOException;
import java.net.SocketException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileClientServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileClientServerApplication.class, args);

		
		 //Util.logMessage("md5: " + Util.getMd5(new File("./downloaded/autopilot_v1.0.zip")));
		MyController mycontroller = new MyController();
		boolean errorOccured =false;
		int stepId=0;
		do {
			errorOccured = false;
			try {
				if (stepId == 0) {
					mycontroller.prepareClientInterfaces();
					stepId++;
				}
				if (stepId == 1) {
					mycontroller.prepareServerInfos();
					stepId++;
				}
				if (stepId == 2) {
					mycontroller.prepareFileListAndChooseFile();
					stepId++;
				}
				if (stepId == 3) {
					mycontroller.prepareDownloadAndDownloadData();
					stepId++;
				}
			} catch (SocketException e) {
				errorOccured = true;
				e.printStackTrace();
			} catch (IOException e) {
				errorOccured = true;
				e.printStackTrace();
			}
		
		
		} while(errorOccured);
	}

}
