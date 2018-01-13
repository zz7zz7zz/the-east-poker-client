package com.open.test.net;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.open.net.client.impl.tcp.bio.BioClient;
import com.open.net.client.structures.BaseClient;
import com.open.net.client.structures.BaseMessageProcessor;
import com.open.net.client.structures.IConnectListener;
import com.open.net.client.structures.TcpAddress;
import com.open.net.client.structures.message.Message;

import java.util.LinkedList;

public class TcpBioClientConnectionActivity extends Activity {

	private BioClient mClient =null;
	private EditText ip,port,sendContent,recContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_socket);
		initView();
		setTitle("java-socket-tcp-bio");
	}


	private void initView()
	{
		findViewById(R.id.set_ip_port).setOnClickListener(listener);
		findViewById(R.id.open).setOnClickListener(listener);
		findViewById(R.id.close).setOnClickListener(listener);
		findViewById(R.id.reconn).setOnClickListener(listener);
		findViewById(R.id.send).setOnClickListener(listener);
		findViewById(R.id.clear).setOnClickListener(listener);

		ip=(EditText) findViewById(R.id.ip);
		port=(EditText) findViewById(R.id.port);
		sendContent=(EditText) findViewById(R.id.sendContent);
		recContent=(EditText) findViewById(R.id.recContent);

		ip.setText("192.168.123.1");
		port.setText("9999");

		mClient = new BioClient(mMessageProcessor,mConnectResultListener);
		mClient.setConnectAddress(new TcpAddress[]{new TcpAddress(ip.getText().toString(), Integer.valueOf(port.getText().toString()))});
	}

	private OnClickListener listener=new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch(v.getId())
			{
				case R.id.set_ip_port:
					mClient.setConnectAddress(new TcpAddress[]{new TcpAddress(ip.getText().toString(), Integer.valueOf(port.getText().toString()))});
					break;

				case R.id.open:
					mClient.connect();
					if(!mClient.isConnected()){
						((TextView)findViewById(R.id.status)).setText("Connectioning");
					}
					break;

				case R.id.close:
					mClient.disconnect();
					((TextView)findViewById(R.id.status)).setText("disconnect");
					break;

				case R.id.reconn:
					mClient.reconnect();
					((TextView)findViewById(R.id.status)).setText("Re-Connectioning");
					break;

				case R.id.send:
					mMessageProcessor.send(mClient,sendContent.getText().toString().getBytes());
					sendContent.setText("");
					break;

				case R.id.clear:
					recContent.setText("");
					break;
			}
		}
	};

	private IConnectListener mConnectResultListener = new IConnectListener() {
		@Override
		public void onConnectionSuccess() {
			runOnUiThread(new Runnable() {
				public void run() {
					((TextView)findViewById(R.id.status)).setText("Connection-Success");
				}
			});
		}

		@Override
		public void onConnectionFailed() {
			runOnUiThread(new Runnable() {
				public void run() {
					((TextView)findViewById(R.id.status)).setText("Connection-Failed");
				}
			});
		}
	};

	private BaseMessageProcessor mMessageProcessor =new BaseMessageProcessor() {

		@Override
		public void onReceiveMessages(BaseClient mClient, final LinkedList<Message> mQueen) {
			for (int i = 0 ;i< mQueen.size();i++) {
				Message msg = mQueen.get(i);
				final String s = new String(msg.data,msg.offset,msg.length);
				runOnUiThread(new Runnable() {
					public void run() {

						recContent.getText().append(s).append("\r\n");
					}
				});
			}
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mClient.disconnect();
	}
}
