package com.yakkuru.korintrain;

import java.io.File;
import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import java.io.IOException;
import java.util.Properties;
import com.yakkuru.korintrain.R;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class KorinTrain extends Activity {
  private static final int PORT = 8765;
  private TextView hello;
  private MyHTTPD server;
  private Handler handler = new Handler();
  MediaPlayer mp = null;
  MediaPlayer mp2 = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_korin_train);
    hello = (TextView) findViewById(R.id.hello);
    mp = MediaPlayer.create(this, R.raw.right); // 正転用
    mp2 = MediaPlayer.create(this, R.raw.left); // 逆転用
    ((Button) findViewById(R.id.buttonGo)).setOnClickListener(buttonGo);
    ((Button) findViewById(R.id.buttonStop)).setOnClickListener(buttonStop);
    ((Button) findViewById(R.id.buttonBack)).setOnClickListener(buttonBack);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  OnClickListener buttonGo = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      OtoPlay();
    }
  };
  OnClickListener buttonStop = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      OtoStop();
    }
  };
  OnClickListener buttonBack = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      // TODO Auto-generated method stub
      BackPlay();
    }
  };

  @Override
  protected void onResume() {
    super.onResume();

    TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
    final String formatedIpAddress = String.format("%d.%d.%d.%d",
        (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
        (ipAddress >> 24 & 0xff));
    textIpaddr.setText("Please access! http://" + formatedIpAddress + ":"
        + PORT);

    try {
      server = new MyHTTPD();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (server != null)
      server.stop();
  }

  private class MyHTTPD extends NanoHTTPD {
    public MyHTTPD() throws IOException {
      super(PORT, null);
    }

    @Override
    public Response serve(String uri, String method, Properties header,
        Properties parms, Properties files) {
      System.out.println(method + " '" + uri + "' ");
      if (uri.equals("/go")) {
        OtoPlay();
        System.out.println("go!!!!");
        return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "go");
      }
      if (uri.equals("/stop")) {
        OtoStop();
        System.out.println("stop!!!!");
        return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "stop");
      }
      if (uri.equals("/back")) {
        BackPlay();
        System.out.println("back!!!!");
        return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "back");
      }
      File index = new File("/mnt/sdcard/");
      return serveFile(uri, header, index, true);
    }
  }

  public void OtoPlay() {
    if (mp2.isPlaying() == false && mp.isPlaying() == false) {
      mp.start();
    }
  }

  public void BackPlay() {
    if (mp.isPlaying() == false && mp2.isPlaying() == false) {
      mp2.start();
    }
  }

  public void OtoStop() {
    if(mp.isPlaying()) {
      mp.pause();
    }
    if(mp2.isPlaying()){
      mp2.pause();
    }
  }
}