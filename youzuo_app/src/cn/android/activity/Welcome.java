package cn.android.activity;

import com.example.youzuo.R;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;


public class Welcome extends Activity {
    /** Called when the activity is first created. */
    //action bar
   
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//      ActionBar mActionBar=getActionBar();
//    	mActionBar.hide();
    	
        setContentView(R.layout.welcome);
		Start();
	}

	public void Start() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Intent intent = new Intent();
				intent.setClass(Welcome.this, Main.class);
				startActivity(intent);
				finish();
			}
		}.start();
	}
}


