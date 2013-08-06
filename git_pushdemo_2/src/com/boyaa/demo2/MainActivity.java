package com.boyaa.demo2;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.boyaa.push.PushSDK;

public class MainActivity extends Activity {

	private EditText sendContent,recContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
	}
	 
	
	private void initView()
	{
		findViewById(R.id.gameIdBtn).setOnClickListener(listener);
		findViewById(R.id.send).setOnClickListener(listener);
		findViewById(R.id.clear).setOnClickListener(listener);
		
		sendContent=(EditText) findViewById(R.id.sendContent);
		recContent=(EditText) findViewById(R.id.recContent);
	}
	
	private OnClickListener listener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			
				case R.id.gameIdBtn:
					PushSDK.getInstance().registerApp(getApplicationContext());
					PushSDK.getInstance().registerPushListener(pushListener);//在你需要回调的地方，注册回调
					
					break;
					
				case R.id.send:
					String content=sendContent.getText().toString();
					if(TextUtils.isEmpty(content))
					{
						Toast.makeText(getApplicationContext(), "内容不能为空", Toast.LENGTH_LONG).show();
						return;
					}
					PushSDK.getInstance().chat(getApplicationContext(),content);
					sendContent.setText("");
					break;
					
				case R.id.clear:
					recContent.setText("");
					break;
			}
		}
	};
	
	
	private Observer pushListener=new Observer() {

		@Override
		public void update(Observable observable, Object data) {
			if(data instanceof String)
			{
				final String ret=(String)data;
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						recContent.getText().append("私聊消息：").append(ret).append("\r\n");
					}
				});
			}

		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			PushSDK.getInstance().unRegisterPushListener(pushListener);
			PushSDK.getInstance().unRegisterApp(getApplicationContext());
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
