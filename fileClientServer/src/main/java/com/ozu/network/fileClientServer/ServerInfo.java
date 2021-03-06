package com.ozu.network.fileClientServer;

import java.io.Serializable;

public class ServerInfo implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 139560260287615734L;
	private Integer id;
    private String ip;
    private int portNumber;

    public ServerInfo(int id, String serverIpPort)
    {
        String[] ipPort = serverIpPort.split(":");
        Util.checkArgument((ipPort != null && ipPort.length == 2), "Server Ip ve port bilgisi ip:port formatinda parametre olarak gonderilmelidir");
        this.id = id;
        this.ip = ipPort[0];
        try
        {
            this.portNumber = Integer.valueOf(ipPort[1]);
        }
        catch (Exception e)
        {
            Util.checkArgument(false, "Port numara tipinde olmalidir");
        }
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public void setPortNumber(int portNumber)
    {
        this.portNumber = portNumber;
    }

    public String getInfo()
    {
        String info = "";
        if (this.id != null)
        {
            info += "Sunucu Id: " + this.id.toString();
        }
        if (this.ip != null)
        {
            info += " IP: " + this.ip;
        }
        if (this.portNumber != 0)
        {
            info += " Port: " + this.portNumber;
        }
        return info;
    }
}
