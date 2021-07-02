package com.example.speedtest.tests;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


public class DownloadTest extends Thread{
    public String DownloadUrl;
    double StartTime;
    double EndTime;
    double TotalTime;
    int BytesDownloaded;
    double DownloadRate;
    double InitialDownloadRate;
    boolean status = false;
    HttpsURLConnection Connection;
    int TimeOut = 5;

    public DownloadTest(String DownloadUrl)
    {
        this.DownloadUrl = DownloadUrl;
    }
    public boolean CurrentStatus()
    {
        return status;
    }

    public void SetInitialDownloadRate(int BytesDownloaded, Double TotalTime)
    {
        if (BytesDownloaded >= 0)
        {
            this.InitialDownloadRate = (Long)Math.round(((BytesDownloaded * 8) / (1000 * 1000)) / TotalTime);
        }
        else
        {
            this.InitialDownloadRate = 0.0;
        }
    }
    public double getInitialDownloadRate()
    {
        return InitialDownloadRate;
    }
    public double getFinalDownloadRate()
    {
        return Math.round(DownloadRate * 100) / 100;
    }
    @Override
    public void run()
    {
        URL url;
        BytesDownloaded = 0;
        int ResponseCode;
        List<String> FileUrls = new ArrayList<String>();
        FileUrls.add(DownloadUrl + "random4000x4000.jpg");
        FileUrls.add(DownloadUrl + "random3000x3000.jpg");
        StartTime = System.currentTimeMillis();
        outer:
        for (String link : FileUrls)
        {
            try
            {
                url = new URL(link);
                Connection = (HttpsURLConnection) url.openConnection();
                Connection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                });
                Connection.connect();
                ResponseCode = Connection.getResponseCode();
            }
            catch (Exception exception)
            {
                break;
            }
            try
            {
                if (ResponseCode == HttpsURLConnection.HTTP_OK)
                {
                    byte[] buffer = new byte[1024];
                    InputStream inputStream = Connection.getInputStream();
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                    {
                        BytesDownloaded += length;
                        EndTime = System.currentTimeMillis();
                        TotalTime = (EndTime - StartTime) / 1000;
                        SetInitialDownloadRate(BytesDownloaded, TotalTime);
                        if (TotalTime >= TimeOut)
                        {
                            break;
                        }
                    }
                    inputStream.close();
                    Connection.disconnect();
                }
                else
                {
                    System.out.println("ERROR: Link not found!");
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        EndTime = System.currentTimeMillis();
        TotalTime = (EndTime - StartTime) / 1000.00;
        DownloadRate = ((BytesDownloaded *8) / (1000 * 1000.0)) / TotalTime;

        status = true;
    }
}
