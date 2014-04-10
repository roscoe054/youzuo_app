package cn.android.activity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.android.model.*;
import cn.android.utils.StreamTool;

import com.example.youzuo.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class MyReservation extends Activity {
	private ListView mListView;
	private SimpleAdapter adapter;
	public static List<Reservation> reservations = new ArrayList<Reservation>();

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_reservation);

		getReservation();

		mListView = (ListView) findViewById(R.id.reservation_list);

		adapter = new SimpleAdapter(this, getData(reservations),
				R.layout.reservation_list_style, new String[] {
						"reservation_key", "reservation_name",
						"reservation_num" }, new int[] { R.id.reservation_key,
						R.id.reservation_name, R.id.reservation_num });
		adapter.notifyDataSetChanged();
		mListView.setAdapter(adapter);

		// action bar
		ActionBar actionBar = this.getActionBar();
		actionBar.setTitle("  我的预定");
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.icon);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionBarColor));

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {// 预定信息长按删除

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View view, int position, long id) {
						final int myPosition = position;
						AlertDialog.Builder builder = new Builder(
								MyReservation.this);
						builder.setTitle("提示");
						builder.setPositiveButton("取消此预约",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										deleteInSet(myPosition);

										reservations.remove(myPosition);
										ListView tempListView = (ListView) findViewById(R.id.reservation_list);
										SimpleAdapter simpleAdapter = new SimpleAdapter(
												MyReservation.this,
												getData(reservations),
												R.layout.reservation_list_style,
												new String[] {
														"reservation_key",
														"reservation_name",
														"reservation_num" },
												new int[] {
														R.id.reservation_key,
														R.id.reservation_name,
														R.id.reservation_num });
										adapter.notifyDataSetChanged();
										tempListView.setAdapter(simpleAdapter);
									}
								});
						builder.create().show();
						return false;
					}
				});
	}

	private List<Map<String, Object>> getData(List<Reservation> reservations) {// 预定列表信息获取
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		// list.clear();
		for (Reservation reservation : reservations) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("reservation_key", reservation.getKey());

			String certainName = reservation.getName();
			if (certainName.length() > 12) {
				certainName = certainName.substring(0, 12) + "...";
			}
			map.put("reservation_name", certainName);
			map.put("reservation_num", "" + reservation.getFrontNum());
			list.add(map);
		}
		return list;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 菜单栏
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) {
			Intent backResList = new Intent();
			backResList.setClass(MyReservation.this, Main.class);
			backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backResList);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {// 返回键监听

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent backResList = new Intent();
			backResList.setClass(MyReservation.this, Main.class);
			backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backResList);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("ShowToast")
	private void getReservation() {
		reservations.clear();
		SQLiteDatabase db = this.openOrCreateDatabase("reservation.db",
				Context.MODE_PRIVATE, null);
		Cursor c = db.rawQuery("select * from Reservation", null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			int indexCode = c.getColumnIndex("code");
			int indexName = c.getColumnIndex("name");
			int indexNum = c.getColumnIndex("num");
			Reservation res = new Reservation(c.getString(indexCode),
					c.getString(indexName), c.getInt(indexNum));
			reservations.add(res);
			c.moveToNext();
		}
		db.close();
	}

	private void deleteInSet(int position) {
		SQLiteDatabase db = this.openOrCreateDatabase("reservation.db",
				Context.MODE_PRIVATE, null);
		String deleteName = reservations.get(position).getName();
		String deleteCode = reservations.get(position).getKey();
		if(deleteInDB(deleteName,deleteCode)){
			db.delete("Reservation", "name=?", new String[] { deleteName });
		}
		db.close();
	}
	
	private boolean deleteInDB(String name,String code) {//提交餐厅名字，返回预定排序号码
		try {
			String path = ResList.PATH + "?key="
					+ URLEncoder.encode("deleteReservation", "UTF-8") + 
					"&name="+ URLEncoder.encode(name, "UTF-8")+"&code="+code;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				// Toast.makeText(getApplicationContext(), "网络连接成功",
				// 2000).show();
				return true;
			}
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), "网络连接失败", 2000).show();
			ex.printStackTrace();
		}
		return false;
	}
}
