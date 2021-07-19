package com.example.speedtest;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speedtest.tests.DownloadTest;
import com.example.speedtest.tests.PingTest;
import com.example.speedtest.tests.UploadTest;
import fr.bmartel.speedtest.SpeedTestSocket;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.*;
import java.util.HashSet;
import java.util.List;

import com.example.speedtest.IpAddress;

public class MainActivity extends AppCompatActivity {


    HashSet<String> BlackList;
    HostServer HostServerHandler;
    StringBuilder stringBuilder = new StringBuilder();

    private boolean ipAddressTestFinished = false;
    private boolean pingTestStarted = false;
    private boolean pingTestFinished = false;
    private boolean downloadTestStarted = false;
    private boolean downloadTestFinished = false;
    private boolean uploadTestStarted = false;
    private boolean uploadTestFinished = false;


    @Override
    public void onResume() {
        super.onResume();
        HostServerHandler = new HostServer();
        HostServerHandler.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DecimalFormat DecFormat = new DecimalFormat("#.##");

        BlackList = new HashSet<>();

        setContentView(R.layout.speedtest_results);

        final Button SpeedTestButton = (Button) findViewById(R.id.startButton);
        HostServerHandler = new HostServer();
        HostServerHandler.start();

        SpeedTestButton.setOnClickListener((View.OnClickListener) v -> {
            SpeedTestButton.setEnabled(false);
            if (HostServerHandler == null) {
                HostServerHandler = new HostServer();
                HostServerHandler.start();
            }
            new Thread(new Runnable() {
                final TextView ipAddressTextView = (TextView) findViewById(R.id.ipAddressNumber);
                final TextView downloadValue = (TextView) findViewById(R.id.downloadSpeed);
                final TextView uploadSpeed = (TextView) findViewById(R.id.uploadSpeed);
                final TextView ping = (TextView) findViewById(R.id.pingNumber);

                @Override
                public void run() {
                    runOnUiThread(() -> SpeedTestButton.setText(R.string.startingSpeedTest));
                    int minute = 600;
                    while (!HostServerHandler.returnStatus()) {
                        minute--;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (minute <= 0) {
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                                SpeedTestButton.setEnabled(true);
                                SpeedTestButton.setText(R.string.restartButtonText);
                            });
                            HostServerHandler = null;
                            return;
                        }
                    }
                    HashMap<Integer, String> key = HostServerHandler.getKey();
                    HashMap<Integer, List<String>> value = HostServerHandler.getValue();
                    double Latitude = HostServerHandler.getLatitude();
                    double Longitude = HostServerHandler.getLongitude();
                    double dist = 0.0;
                    int ServerIndex = 0;
                    double temp = 19349458;
                    BlackList = new HashSet<String>();

                    for (int index : key.keySet()) {
                        if (BlackList.contains(value.get(index).get(3))) {
                            continue;
                        }
                        Location source = new Location("Source");
                        source.setLongitude(Longitude);
                        source.setLatitude(Latitude);
                        List<String> list = value.get(index);
                        Location destination = new Location("Dest");
                        destination.setLatitude(Double.parseDouble(list.get(0)));
                        destination.setLongitude(Double.parseDouble(list.get(1)));

                        double distance = source.distanceTo(destination);
                        if (temp > distance) {
                            temp = distance;
                            dist = distance;
                            ServerIndex = index;
                        }
                    }
                    String testAddress = key.get(ServerIndex).replace("http://", "https://");
                    final List<String> info = value.get(ServerIndex);
                    final double distance = dist;
                    if (info == null) {
                        runOnUiThread(() -> SpeedTestButton.setText("Error getting Host Location."));
                        return;
                    }

                    runOnUiThread(() -> SpeedTestButton.setText("Location: " + info.get(2)));

                    final List<Double> pingRate = new ArrayList<Double>();
                    final List<Double> downloadRate = new ArrayList<Double>();

                    final PingTest pingTest = new PingTest(info.get(6).replace(":8800", ""), 1);
                    final DownloadTest downloadTest = new DownloadTest(testAddress.replace(testAddress.split("/")[testAddress.split("/").length - 1], ""));
                    final UploadTest uploadTest = new UploadTest(testAddress);

                    runOnUiThread(() -> ipAddressTextView.setText(IpAddress.getIPAddress(true)));

                    while (true)
                    {
                        if (!pingTestStarted)
                        {
                            pingTest.start();
                            pingTestStarted = true;
                        }
                        if (pingTestFinished && !downloadTestStarted)
                        {
                            downloadTest.start();
                            downloadTestStarted = true;
                        }
                        if (downloadTestFinished && !uploadTestStarted)
                        {
                            uploadTest.start();
                            uploadTestStarted = true;
                        }
                        // Ping Test Calculations
                        if (pingTestFinished)
                        {
                            if (pingTest.getAverageRoundTripTime() == 0)
                            {
                                runOnUiThread(() -> ping.setText("Error"));

                            }
                            else
                            {
                                runOnUiThread(() -> ping.setText(DecFormat.format(pingTest.getAverageRoundTripTime()) + " ms"));
                            }
                        }
                        else
                        {
                            pingRate.add(pingTest.getInstantRoundTripTime());
                            runOnUiThread(() -> ping.setText(DecFormat.format(pingTest.getInstantRoundTripTime()) + " ms"));
                        }

                        // Download Test Calculations
                        if (pingTestFinished)
                        {
                            if (downloadTestFinished)
                            {
                                if (downloadTest.getFinalDownloadRate() == 0)
                                {
                                    runOnUiThread(() -> downloadValue.setText("Error"));
                                }

                            }
                            else
                            {
                                runOnUiThread(() -> downloadValue.setText(downloadTest.getFinalDownloadRate() + " mbps"));
                            }
                        }

                        // Upload Speed Calculations
                        if (downloadTestFinished)
                        {
                            if (uploadTestFinished)
                            {
                                if (uploadTest.getFinalDownloadRate() == 0)
                                    runOnUiThread(() -> uploadSpeed.setText("Upload Speed Error"));
                            }
                            else
                            {
                                runOnUiThread(() -> uploadSpeed.setText(uploadTest.getFinalDownloadRate() + " mbpss"));
                            }
                        }
                        if (pingTestFinished && downloadTestFinished && uploadTestFinished)
                        {
                            break;
                        }
                        if (pingTest.isFinished())
                        {
                            pingTestFinished = true;
                        }
                        if (downloadTest.CurrentStatus())
                        {
                            downloadTestFinished = true;
                        }
                        if (uploadTest.CurrentStatus())
                        {
                            uploadTestFinished = true;
                        }
                        try {
                            Thread.sleep(500);
                        }
                        catch (InterruptedException interruptedException)
                        {
                            interruptedException.printStackTrace();
                        }

                    }
                    runOnUiThread(() -> {
                        SpeedTestButton.setEnabled(true);
                        SpeedTestButton.setText(R.id.startButton);
                    });

                    setContentView(R.layout.speedtest_results);
                }
            }).start();

        });
    }
}
