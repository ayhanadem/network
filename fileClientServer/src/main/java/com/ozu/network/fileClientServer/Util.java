package com.ozu.network.fileClientServer;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;


public class Util
{
    public static String getMd5(File file)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];
            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1)
            {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++)
            {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void checkArgument(boolean state, String message)
    {
    	if(state ==false)
    	{
    		System.out.println(message);
    	}
    }
    
    public static void logMessage(String message)
    {
    	System.out.println(message);
    }
}
