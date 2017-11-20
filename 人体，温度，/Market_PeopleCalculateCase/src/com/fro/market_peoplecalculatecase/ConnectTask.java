package com.fro.market_peoplecalculatecase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.fro.util.FRODigTube;
import com.fro.util.FROSmoke;
import com.fro.util.FROTemHum;
import com.fro.util.FROBody;
import com.fro.util.StreamUtil;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Context context;
	TextView body_tv;
	TextView info_tv;

	private Boolean body;
	private byte[] read_buff;
	private int tem;
	private int hem;

	private Socket bodySocket;
	private Socket tubeSocket;
	private Socket temhemSocket;
	private Socket fanSocket;
	 TextView tem_tv;
	 TextView hem_tv;
	 String name ="无人";
	private boolean CIRCLE = false;

	private boolean isDialogShow = false;

	public ConnectTask(Context context, TextView body_tv, TextView info_tv, TextView tem_tv, TextView hem_tv) {
		this.context = context;
		this.body_tv = body_tv;
		this.info_tv = info_tv;
		this.tem_tv = tem_tv;
		this.hem_tv = hem_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (bodySocket != null && temhemSocket!=null) {
			// if (bodySocket != null ) {
			info_tv.setTextColor(context.getResources().getColor(R.color.green));
			info_tv.setText("连接正常！");
		} else {
			info_tv.setTextColor(context.getResources().getColor(R.color.red));
			info_tv.setText("连接失败！");
		}

		// 显示数据
		if (Const.body != null) {
			body_tv.setText(name);
		}
		tem_tv.setText(String.valueOf(tem));
		hem_tv.setText(String.valueOf(hem));

	}

	/**
	 * 子线程任务
	 * 
	 * @param params
	 * @return
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// 连接
		bodySocket = getSocket(Const.BODY_IP, Const.BODY_PORT);
		//beSocket = getSocket(Const.TUBE_IP, Const.TUBE_PORT);
		fanSocket = getSocket("192.168.0.105", 4001);
		temhemSocket = getSocket("192.168.0.100", 4001);
		
		// 循环读取数据
		while (CIRCLE) {
			// 如果全部连接成功
			// if (bodySocket != null ) {
			if (bodySocket != null && temhemSocket != null) {
				try {
					Log.i(Const.TAG,  "1");
					// 估计客流值(不停监测，一旦检测到有人则累加1)
					StreamUtil.writeCommand(bodySocket.getOutputStream(), Const.BODY_CHK);
					Thread.sleep(1000);
					read_buff = StreamUtil.readData(bodySocket.getInputStream());
					body = FROBody.getData(Const.BODY_LEN, Const.BODY_NUM, read_buff);
					Log.i(Const.TAG,  String.valueOf(body));
					if (body == true) {
						name="有人";
					}else{
						name = "无人";
					}
					Log.i(Const.TAG,  "2");
					StreamUtil.writeCommand(temhemSocket.getOutputStream(), "01 03 00 14 00 02 84 0f");
					Thread.sleep(1000);
					Log.i(Const.TAG,  "4");
					read_buff =StreamUtil.readData(temhemSocket.getInputStream());
					Log.i(Const.TAG,  "5");
					float temnum = FROTemHum.getTemData(9,1, read_buff);
					tem = (int)(float)temnum;
					Log.i(Const.TAG,  "6");
					hem = (int)(float)FROTemHum.getHumData(9, 1, read_buff);
					Log.i(Const.TAG,  "3");
					// 数码管显示
//					Const.TUBE_CMD = FRODigTube.intToCmdString(Const.body);
//					StreamUtil.writeCommand(tubeSocket.getOutputStream(), Const.TUBE_CMD);
//					Thread.sleep(200);

					// 输出客流数
//					Log.i(Const.TAG, "Const.body=" + Const.body);
					if((tem>2)&&(name=="有人")){
						StreamUtil.writeCommand(fanSocket.getOutputStream(), "01 10 00 48 00 01 02 00 01 68 18");
						Thread.sleep(800);
					}
					
					if((tem<0)||(name=="无人")){
						StreamUtil.writeCommand(fanSocket.getOutputStream(),"01 10 00 48 00 01 02 00 02 28 19");
						Thread.sleep(800);
					}
					Log.i(Const.TAG,  "5");
					// 更新界面
					publishProgress();
					Thread.sleep(200);

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 建立连接并返回socket，若连接失败返回null
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Socket getSocket(String ip, int port) {
		Socket mSocket = new Socket();
		InetSocketAddress mSocketAddress = new InetSocketAddress(ip, port);
		// socket连接
		try {
			// 设置连接超时时间为3秒
			mSocket.connect(mSocketAddress, 3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 检查是否连接成功
		if (mSocket.isConnected()) {
			Log.i(Const.TAG, ip + "连接成功！");
			return mSocket;
		} else {
			Log.i(Const.TAG, ip + "连接失败！");
			return null;
		}
	}

	public void setCIRCLE(boolean cIRCLE) {
		CIRCLE = cIRCLE;
	}

	@Override
	protected void onCancelled() {
		info_tv.setTextColor(context.getResources().getColor(R.color.gray));
		info_tv.setText("请点击连接！");
	}

}
