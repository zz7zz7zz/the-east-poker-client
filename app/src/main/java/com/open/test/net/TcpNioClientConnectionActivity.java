package com.open.test.net;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.open.net.client.impl.tcp.nio.NioClient;
import com.open.net.client.object.AbstractClient;
import com.open.net.client.object.IConnectListener;
import com.open.net.client.object.TcpAddress;
import com.poker.cmd.LoginCmd;
import com.poker.data.DataPacket;
import com.poker.packet.BasePacket;
import com.poker.packet.InPacket;
import com.poker.packet.OutPacket;
import com.poker.protocols.login.client.LoginClient;
import com.poker.protocols.login.server.ResponseLoginProto;


public class TcpNioClientConnectionActivity extends Activity {

	private static final String TAG = "TcpNioClient";

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

		ip.setText("192.168.123.1");
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

	public static byte[] mTempBuff = new byte[8*1024];
	private IConnectListener mConnectResultListener = new IConnectListener() {
		@Override
		public void onConnectionSuccess(AbstractClient abstractClient) {
			runOnUiThread(new Runnable() {
				public void run() {
					((TextView)findViewById(R.id.status)).setText("Connection-Success");
				}
			});

			byte[] requestData = LoginClient.requestLogin("1111",0);
			int length = BasePacket.buildClientPacekt(mTempBuff,1,LoginCmd.CMD_LOGIN_REQUEST,(byte)0,requestData,0,requestData.length);
			mMessageProcessor.send(mClient,mTempBuff,0, length);
			System.out.println("onConnectionSuccess");
		}

		@Override
		public void onConnectionFailed(AbstractClient abstractClient) {
			runOnUiThread(new Runnable() {
				public void run() {
					((TextView)findViewById(R.id.status)).setText("Connection-Failed");
				}
			});
		}
	};


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mClient.disconnect();
	}

	private ClientHandler mMessageProcessor = new ClientHandler(new InPacket(8192),new OutPacket(8192));
	//----------------------------------------------------------------------------------------------------
	public class ClientHandler extends AbsClientHandler{

		public ClientHandler(InPacket mInPacket, OutPacket mOutPacket) {
			super(mInPacket, mOutPacket);
		}

		public void dispatchMessage(AbstractClient client ,byte[] data,int header_start,int header_length,int body_start,int body_length){

			try {
				int cmd   = DataPacket.getCmd(data, header_start);
				System.out.println("input_packet cmd 0x" + Integer.toHexString(cmd) + " name " + LoginCmd.getCmdString(cmd) + " length " + DataPacket.getLength(data,header_start));

				if(cmd == LoginCmd.CMD_LOGIN_RESPONSE){
					onResponseLogin(data,header_start,header_length,body_start,body_length);
				}

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}

		public void onResponseLogin(byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			ResponseLoginProto.ResponseLogin readObj = ResponseLoginProto.ResponseLogin.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {

					recContent.getText().append(s).append("\r\n");
				}
			});

		}
	}
}
