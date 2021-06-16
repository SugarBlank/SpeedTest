package com.example.speedtest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HostServer extends Thread {
    HashMap<Integer, String> key = new HashMap<Integer, String>();
    HashMap<Integer, List<String>> value = new HashMap<Integer, List<String>>();
    boolean status = false;
    double Latitude;
    double Longitude;
    String Lat;
    String Lon;
    int count = 0;
    String Address;
    String Name;
    String Country;

    public HashMap<Integer, String> getKey() {
        return key;
    }
    public HashMap<Integer, List<String>> getValue() {
        return value;
    }
    public double getLatitude() {
        return Latitude;
    }
    public double getLongitude() {
        return Longitude;
    }
    public boolean returnStatus() {
        return status;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("https://www.speedtest.net/speedtest-config.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int code = urlConnection.getResponseCode();
            if (code == 200) {
                BufferedReader Reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = Reader.readLine()) != null) {
                    if (!line.contains("isp=")) {
                        continue;
                    }
                    Longitude = Double.parseDouble(line.split("lon=\"")[1].split(" ")[0].replace("\"", ""));
                    Latitude = Double.parseDouble(line.split("lat=\"")[1].split(" ")[0].replace("\"", ""));
                    break;
                }
                Reader.close();
            }
        } catch (Exception FailedConnection) {
            FailedConnection.printStackTrace();
        }
        status = true;


        try {
            URL url = new URL("https://www.speedtest.net/speedtest-servers-static.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int code = urlConnection.getResponseCode();
            if (code == 200) {
                BufferedReader Reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = Reader.readLine()) != null) {
                    if (line.contains("<server url")) {
                        Address = line.split("server url=\"")[1].split("\"")[0];
                        Name = line.split("name=\"")[1].split("\"")[0];
                        Country = line.split("country=\"")[1].split("\"")[0];
                        Lat = line.split("lat=\"")[1].split("\"")[0];
                        Lon = line.split("lon=\"")[1].split("\"")[0];
                        List<String> ls = Arrays.asList(Lat, Lon, Name, Country);
                        key.put(count, Address);
                        value.put(count, ls);
                        count++;
                    }
                }
                Reader.close();
            }

        }
        catch (Exception FailedConnection) {
            FailedConnection.printStackTrace();
        }
        status = true;
    }
}
