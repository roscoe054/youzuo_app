package cn.android.activity;

import cn.android.model.Reservation;
import cn.android.utils.StreamTool;

import com.example.youzuo.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.view.View.OnClickListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class ResInfo extends Activity {

	private TextView evaT;
	private TextView waitingT;
	private TextView locationT;
	private TextView telT;
	private TextView introT;
	private String name;
	private int evaluation;
	private int waitingNum;
	private String code;
	private String location;
	private String tel;
	private String introduct;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.res_info);

		// get res info
		Intent getResList = this.getIntent();

		//获取和显示数据
		name = getResList.getStringExtra("name");
		evaluation = getResList.getIntExtra("evaluation", 0);
		waitingNum = getResList.getIntExtra("waitingNum", 0);
		location = getResList.getStringExtra("location");
		tel = getResList.getStringExtra("tel");
		introduct = getResList.getStringExtra("introduct");

		evaT = (TextView) findViewById(R.id.info_evaluation);
		waitingT = (TextView) findViewById(R.id.info_waitingnum);
		locationT = (TextView) findViewById(R.id.info_location);
		telT = (TextView) findViewById(R.id.info_tel);
		introT = (TextView) findViewById(R.id.info_introduct);

		evaT.setText(getStars(evaluation));
		waitingT.setText("" + waitingNum);
		locationT.setText(location);
		telT.setText(tel);
		introT.setText("简介 : " + introduct);

		// action bar
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		String shortName = name;
		if (shortName.length() > 12) {
			shortName = shortName.substring(0, 12) + "...";
		}
		actionBar.setTitle("  " + shortName);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.icon);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionBarColor));

		Button btBook = (Button) findViewById(R.id.bt_book);

		btBook.setOnClickListener(new OnClickListener() {//订阅按钮监听

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!isBooked()) {
					dialog(getCode());
				} else {
					dialog("isBooked");
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//菜单栏
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) {
			Intent backResList = new Intent();
			backResList.setClass(ResInfo.this, ResList.class);
			backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backResList);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {//返回键监听

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent backResList = new Intent();
			backResList.setClass(ResInfo.this, ResList.class);
			backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backResList);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void dialog(String Code) {//点击预定键生成的对话框
		// String randomKey = getCharAndNumr(2);
		boolean isBooked=false;
		if(Code.equals("isBooked"))isBooked=true;
		
		AlertDialog.Builder builder = new Builder(ResInfo.this);
		
		
		if (isBooked) {
			builder.setTitle("提示");
			builder.setMessage("  您已经预定了该餐厅");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
		} else {
			builder.setTitle("提示");
			builder.setMessage("  预定成功，您的序号为" + Code + "\n  可在我的预定中查看");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
//			Reservation myRervation = new Reservation(Code, name, waitingNum);
//			MyReservation.reservations.add(myRervation);
		}
		builder.create().show();
	}

	private String getCode() {//提交餐厅名字，返回预定排序号码
		try {
			String path = ResList.PATH + "?key="
					+ URLEncoder.encode("reservation", "UTF-8") + "&name="
					+ URLEncoder.encode(name, "UTF-8");
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				// Toast.makeText(getApplicationContext(), "网络连接成功",
				// 2000).show();
				byte[] byteReservationCode = StreamTool.readInputStream(conn
						.getInputStream());
				String ReservationCode = new String(byteReservationCode);
				code=ReservationCode;
				saveReservation();//本地存储预定信息
				return ReservationCode;
			}
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), "网络连接失败", 2000).show();
			ex.printStackTrace();
		}
		return null;
	}

	private Boolean isBooked() {//判断该餐厅是否已预定
		SQLiteDatabase db =this.openOrCreateDatabase("reservation.db", Context.MODE_PRIVATE, null);
		Cursor c = db.rawQuery("select name from Reservation", null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			int indexName = c.getColumnIndex("name");
			if(c.getString(indexName).equals(name)){
				return true;
			}
			c.moveToNext();
		}
		db.close();
		return false;
	}

	private String getStars(int eva) {
		String stars = null;
		switch (eva) {
		case 0:
			break;
		case 1:
			stars = "★";
			break;
		case 2:
			stars = "★★";
			break;
		case 3:
			stars = "★★★";
			break;
		case 4:
			stars = "★★★★";
			break;
		case 5:
			stars = "★★★★★";
			break;
		default:
			break;
		}
		return stars;
	}
	
	private void saveReservation(){
		SQLiteDatabase db =this.openOrCreateDatabase("reservation.db", Context.MODE_PRIVATE, null);
		ContentValues values =new ContentValues();
		values.put("code", code);
		db.insert("reservation", "code", values);
		values.put("name", name);
		db.insert("reservation", "name", values);
		values.put("num", waitingNum);
		db.insert("reservation", "num", values);
		db.close();
	}
}
