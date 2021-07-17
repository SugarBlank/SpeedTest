package com.example.speedtest.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.HashMap;

public class PingTest extends Thread{
    HashMap<String, Object> result = new HashMap<String, Object>();
    String server;
    int count;
    double instantRoundTripTime;
    double averageRoundTripTime;
    boolean finished = false;
    boolean started = false;
    Process process;

    public PingTest(String serverIpAddress, int AttemptCount)
    {
        this.server = serverIpAddress;
        this.count = AttemptCount;
    }
    public double getAverageRoundTripTime()
    {
        return averageRoundTripTime;
    }
    public double getInstantRoundTripTime()
    {
        return instantRoundTripTime;
    }
    public boolean isFinished()
    {
        return finished;
    }
    @Override
    public void run()
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder("ping", "-c" + count, "google.com");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("icmp_seq"))
                {
                    instantRoundTripTime = Double.parseDouble(line.split(" ")[line.split(" ").length - 2].replace("time=", ""));
                }
                if (line.startsWith("rtt "))
                {
                    averageRoundTripTime = Double.parseDouble(line.split("/")[4]);
                    break;
                }
                if (line.contains("Unreachable") || line.contains("Unknown") || line.contains("%100 packet loss"))
                {
                    return;
                }
            }
            process.waitFor();
            bufferedReader.close();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finished = true;
    }
}
