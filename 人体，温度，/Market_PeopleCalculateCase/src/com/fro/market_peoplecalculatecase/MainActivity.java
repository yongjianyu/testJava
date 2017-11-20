package com.fro.market_peoplecalculatecase;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private Context context;

	private EditText bodyIp_et;
	private EditText bodyPort_et;
	private EditText tubeIp_et;
	private EditText tubePort_et;
	
	private TextView body_tv;
	private ToggleButton connect_tb;
	private TextView info_tv,tem_tv,hem_tv;
	
	private ConnectTask connectTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context=this;

		// 绑定控件
		bindView();
		// 初始化数据
		initData();
		// 事件监听
		initEvent();
	}
	
	/**
	 * 绑定控件
	 */
	private void bindView() {
		bodyIp_et = (EditText) findViewById(R.id.bodyIp_et);
		bodyPort_et = (EditText) findViewById(R.id.bodyPort_et);
		tubeIp_et = (EditText) findViewById(R.id.tubeIp_et);
		tubePort_et = (EditText) findViewById(R.id.tubePort_et);
		
		connect_tb = (ToggleButton) findViewById(R.id.connect_tb);
		info_tv = (TextView) findViewById(R.id.info_tv);
		body_tv = (TextView) findViewById(R.id.body_tv);
		tem_tv = (TextView) findViewById(R.id.tem_tv);
		hem_tv= (TextView) findViewById(R.id.hem_tv);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		bodyIp_et.setText(Const.BODY_IP);
		bodyPort_et.setText(String.valueOf(Const.BODY_PORT));
		tubeIp_et.setText(Const.TUBE_IP);
		tubePort_et.setText(String.valueOf(Const.TUBE_PORT));
		
	}

	/**
	 * 按钮监听
	 */
	private void initEvent() {
		
		//连接
		connect_tb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// 获取IP和端口
					Const.BODY_IP = bodyIp_et.getText().toString().trim();
					Const.BODY_PORT = Integer.parseInt(bodyPort_et.getText().toString().trim());
					Const.TUBE_IP = tubeIp_et.getText().toString().trim();
					Const.TUBE_PORT = Integer.parseInt(tubePort_et.getText().toString().trim());
					// 开启任务
					connectTask = new ConnectTask(context, body_tv, info_tv,tem_tv,hem_tv);
					connectTask.setCIRCLE(true);
					connectTask.execute();
				} else {
					try {
						// 取消任务
						if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
							connectTask.setCIRCLE(false);
							Thread.sleep(2*Const.time);
							
							// 如果Task还在运行，则先取消它
							connectTask.cancel(true);
							info_tv.setTextColor(context.getResources().getColor(R.color.gray));
							info_tv.setText("请点击连接！");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	
	@Override
	public void finish() {
		super.finish();
		try {
			// 取消任务
			if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
				connectTask.setCIRCLE(false);
				Thread.sleep(2*Const.time);
				// 如果Task还在运行，则先取消它
				connectTask.cancel(true);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
