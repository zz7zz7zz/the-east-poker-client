package com.open.test.net;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.protobuf.InvalidProtocolBufferException;
import com.open.net.client.impl.tcp.nio.NioClient;
import com.open.net.client.structures.BaseClient;
import com.open.net.client.structures.BaseMessageProcessor;
import com.open.net.client.structures.IConnectListener;
import com.open.net.client.structures.TcpAddress;
import com.open.net.client.structures.message.Message;
import com.poker.data.DataPacket;
import com.poker.protocols.Login;
import com.poker.protocols.server.LoginResponseProto;

import java.util.LinkedList;

public class TcpNioClientConnectionActivity extends Activity {

	private NioClient mClient =null;
	private EditText ip,port,sendContent,recContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_socket);
		initView();
		setTitle("java-socket-tcp-nio");
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

//		ip.setText("192.168.4.114");
//		port.setText("10010");

		ip.setText("192.168.9.141");
		port.setText("10010");

		mClient = new NioClient(mMessageProcessor,mConnectResultListener);
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
					break;
					
				case R.id.close:
					mClient.disconnect();
					break;
					
				case R.id.reconn:
					mClient.reconnect();
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

	public static byte[] write_buff = new byte[16*1024];
	private IConnectListener mConnectResultListener = new IConnectListener() {
		@Override
		public void onConnectionSuccess() {
			int length = Login.login(write_buff,1000,"1111",0);
			mMessageProcessor.send(mClient,write_buff,0, length);
			System.out.println("onConnectionSuccess");
		}

		@Override
		public void onConnectionFailed() {
			System.out.println("onConnectionFailed");
		}
	};

	private BaseMessageProcessor mMessageProcessor =new BaseMessageProcessor() {

		@Override
		public void onReceiveMessages(BaseClient mClient, final LinkedList<Message> mQueen) {
			for (int i = 0 ;i< mQueen.size();i++) {
				Message msg = mQueen.get(i);
				try {
					final String s1 = new String(msg.data,msg.offset,msg.length);
					LoginResponseProto.LoginResponse readObj = LoginResponseProto.LoginResponse.parseFrom(msg.data,msg.offset+ DataPacket.getHeaderLength(),msg.length-DataPacket.getHeaderLength());
					final String s2 = readObj.toString();
					runOnUiThread(new Runnable() {
						public void run() {

							recContent.getText().append(s1).append("\r\n");

							recContent.getText().append(s2).append("\r\n");
						}
					});
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

			}

		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mClient.disconnect();
	}
}
