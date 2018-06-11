package com.open.test.net;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;
import com.open.net.client.impl.tcp.nio.NioClient;
import com.open.net.client.object.AbstractClient;
import com.open.net.client.object.IConnectListener;
import com.open.net.client.object.TcpAddress;
import com.poker.base.GameIds;
import com.poker.cmd.BaseGameCmd;
import com.poker.cmd.Cmd;
import com.poker.cmd.LoginCmd;
import com.poker.cmd.SystemCmd;
import com.poker.cmd.UserCmd;
import com.poker.data.DataPacket;
import com.poker.packet.BasePacket;
import com.poker.packet.InPacket;
import com.poker.packet.OutPacket;
import com.poker.protocols.GameClient;
import com.poker.protocols.LoginClient;
import com.poker.protocols.TexasCmd;
import com.poker.protocols.TexasGameClient;
import com.poker.protocols.game.server.BroadcastUserExitProto.BroadcastUserExit;
import com.poker.protocols.game.server.BroadcastUserOfflineProto.BroadcastUserOffline;
import com.poker.protocols.game.server.BroadcastUserReadyProto.BroadcastUserReady;
import com.poker.protocols.login.server.ResponseLoginProto;
import com.poker.protocols.texaspoker.BroadcastUserLoginProto.BroadcastUserLogin;
import com.poker.protocols.texaspoker.TexasGameBroadcastNextOperateProto.TexasGameBroadcastNextOperate;
import com.poker.protocols.texaspoker.TexasGameBroadcastPotProto.TexasGameBroadcastPot;
import com.poker.protocols.texaspoker.TexasGameBroadcastUserActionProto.TexasGameBroadcastUserAction;
import com.poker.protocols.texaspoker.TexasGameDealFlopProto.TexasGameDealFlop;
import com.poker.protocols.texaspoker.TexasGameDealPreFlopProto.TexasGameDealPreFlop;
import com.poker.protocols.texaspoker.TexasGameDealRiverProto.TexasGameDealRiver;
import com.poker.protocols.texaspoker.TexasGameDealTurnProto.TexasGameDealTurn;
import com.poker.protocols.texaspoker.TexasGameEndProto.TexasGameEnd;
import com.poker.protocols.texaspoker.TexasGameErrorProto.TexasGameError;
import com.poker.protocols.texaspoker.TexasGameReconnectProto.TexasGameReconnect;
import com.poker.protocols.texaspoker.TexasGameResponseLoginGameProto.TexasGameResponseLoginGame;
import com.poker.protocols.texaspoker.TexasGameShowHandProto.TexasGameShowHand;
import com.poker.protocols.texaspoker.TexasGameStartProto.TexasGameStart;

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
		findViewById(R.id.heat_beat).setOnClickListener(listener);

		findViewById(R.id.send).setOnClickListener(listener);
		findViewById(R.id.clear).setOnClickListener(listener);

		findViewById(R.id.login).setOnClickListener(listener);
		findViewById(R.id.login_game).setOnClickListener(listener);

		findViewById(R.id.fold).setOnClickListener(listener);
		findViewById(R.id.check).setOnClickListener(listener);
		findViewById(R.id.call).setOnClickListener(listener);
		findViewById(R.id.raise).setOnClickListener(listener);

		ip=(EditText) findViewById(R.id.ip);
		port=(EditText) findViewById(R.id.port);
		sendContent=(EditText) findViewById(R.id.sendContent);
		recContent=(EditText) findViewById(R.id.recContent);

//		ip.setText("192.168.123.1");
		ip.setText("10.0.2.2");
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

				case R.id.login:
					{
						byte[] data = LoginClient.requestLogin(((EditText)findViewById(R.id.uuid)).getText().toString(),0);
						int length = BasePacket.buildClientPacekt(mTempBuff,1,LoginCmd.CMD_LOGIN_REQUEST,(byte)0,data,0,data.length);
						mMessageProcessor.send(mClient,mTempBuff,0, length);
					}
                    break;

				case R.id.login_game:
					{
						byte[] data = GameClient.requestLogin(GameIds.TEXAS_HOLD_EM_POKER,1);
						int length = BasePacket.buildClientPacekt(mTempBuff,1, UserCmd.CMD_LOGIN_GAME,(byte)0,data,0,data.length);
						mMessageProcessor.send(mClient,mTempBuff,0, length);
					}
					break;

				case R.id.fold:
				{
					byte[] data = TexasGameClient.action(1,0);
					int length = BasePacket.buildClientPacekt(mTempBuff,1, TexasCmd.CMD_CLIENT_ACTION,(byte)0,data,0,data.length);
					mMessageProcessor.send(mClient,mTempBuff,0, length);
				}
				break;
				case R.id.check:
				{
					byte[] data = TexasGameClient.action(2,0);
					int length = BasePacket.buildClientPacekt(mTempBuff,1, TexasCmd.CMD_CLIENT_ACTION,(byte)0,data,0,data.length);
					mMessageProcessor.send(mClient,mTempBuff,0, length);
				}
				break;

				case R.id.call:
				{
					byte[] data = TexasGameClient.action(4,0);
					int length = BasePacket.buildClientPacekt(mTempBuff,1, TexasCmd.CMD_CLIENT_ACTION,(byte)0,data,0,data.length);
					mMessageProcessor.send(mClient,mTempBuff,0, length);
				}
				break;

				case R.id.raise:
				{
					String s_raise_chip = ((EditText)findViewById(R.id.raise_chip)).getText().toString();
					if(!TextUtils.isEmpty(s_raise_chip)){
						long raise_chip = Integer.valueOf(s_raise_chip);
						byte[] data = TexasGameClient.action(8,raise_chip);
						int length = BasePacket.buildClientPacekt(mTempBuff,1, TexasCmd.CMD_CLIENT_ACTION,(byte)0,data,0,data.length);
						mMessageProcessor.send(mClient,mTempBuff,0, length);
					}else{
						Toast.makeText(getApplicationContext(),"chip must > 0 ",Toast.LENGTH_SHORT).show();
					}
				}
				break;

				case R.id.heat_beat:
				{
					byte[] EMPTY_BYTE_ARRAY= new byte[0];
					int length = BasePacket.buildClientPacekt(mTempBuff,1, SystemCmd.CMD_SYS_HEAR_BEAT,(byte)0,EMPTY_BYTE_ARRAY,0,0);
					mMessageProcessor.send(mClient,mTempBuff,0, length);
				}

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
				System.out.println("input_packet cmd 0x" + Integer.toHexString(cmd) + " name " + Cmd.getCmdString(cmd) + " length " + DataPacket.getLength(data,header_start));
				if(cmd == SystemCmd.CMD_SYS_HEAR_BEAT_REPONSE){

				}else if(cmd == LoginCmd.CMD_LOGIN_RESPONSE){
					onResponseLogin(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == BaseGameCmd.CMD_SERVER_USERLOGIN){
					onResponseLoginGame(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == BaseGameCmd.CMD_SERVER_BROAD_USERLOGIN){
					onBroadUserLogin(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == BaseGameCmd.CMD_SERVER_BROAD_USERLOGOUT){
					onBroadcastUserExit(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == BaseGameCmd.CMD_SERVER_BROAD_USERREADY){
					onBroadcastUserReady(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == BaseGameCmd.CMD_SERVER_BROAD_USEROFFLINE){
					onOffLineGame(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_GAME_START){
					onGameStart(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_DEAL_PREFLOP){
					onDealPreFlop(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_DEAL_FLOP){
					onDealFlop(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_DEAL_TURN){
					onDealTurn(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_DEAL_RIVER){
					onDealRiver(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_BROADCAST_USER_ACTION){
					onBroadcastUserAction(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_BROADCAST_NEXT_OPERATE){
					onBroadcastNextOperateUser(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_BROADCAST_POTS){
					onBroadcastPots(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_BROADCAST_SHOW_HAND){
					onBroadcastShowHand(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_GAME_OVER){
					onBroadcastGameOver(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_RECONNECT){
					onGameReconnect(cmd,data,header_start,header_length,body_start,body_length);
				}else if(cmd == TexasCmd.CMD_SERVER_USER_ERROR){
					onGameError(cmd,data,header_start,header_length,body_start,body_length);
				}else{
					System.out.println("input_packet err -------------------- not handled ");
				}

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
				System.out.println("input_packet err -------------------- exception ");
			}
		}

		public void onResponseLogin(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			ResponseLoginProto.ResponseLogin readObj = ResponseLoginProto.ResponseLogin.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onResponseLoginGame(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameResponseLoginGame readObj = TexasGameResponseLoginGame.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadUserLogin(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			BroadcastUserLogin readObj = BroadcastUserLogin.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastUserExit(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			BroadcastUserExit readObj = BroadcastUserExit.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastUserReady(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			BroadcastUserReady readObj = BroadcastUserReady.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onOffLineGame(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			BroadcastUserOffline readObj = BroadcastUserOffline.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onGameStart(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameStart readObj = TexasGameStart.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
                    recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onDealPreFlop(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameDealPreFlop readObj = TexasGameDealPreFlop.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onDealFlop(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameDealFlop readObj = TexasGameDealFlop.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}


		public void onDealTurn(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameDealTurn readObj = TexasGameDealTurn.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onDealRiver(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameDealRiver readObj = TexasGameDealRiver.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastUserAction(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameBroadcastUserAction readObj = TexasGameBroadcastUserAction.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastNextOperateUser(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameBroadcastNextOperate readObj = TexasGameBroadcastNextOperate.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastPots(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameBroadcastPot readObj = TexasGameBroadcastPot.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastShowHand(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameShowHand readObj = TexasGameShowHand.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onBroadcastGameOver(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameEnd readObj = TexasGameEnd.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onGameReconnect(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameReconnect readObj = TexasGameReconnect.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}

		public void onGameError(final int cmd, byte[] data, int header_start, int header_length, int body_start, int body_length) throws InvalidProtocolBufferException {
			TexasGameError readObj = TexasGameError.parseFrom(data,body_start,body_length);
			final String s = readObj.toString();
			runOnUiThread(new Runnable() {
				public void run() {
					recContent.getText().append("---0x"+Integer.toHexString(cmd)+"---").append("\r\n").append(s).append("\r\n");
					recContent.setMovementMethod(ScrollingMovementMethod.getInstance());
					recContent.setSelection(recContent.getText().length(), recContent.getText().length());
				}
			});

		}
	}
}
