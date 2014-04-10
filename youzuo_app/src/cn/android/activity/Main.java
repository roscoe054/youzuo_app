package cn.android.activity;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import cn.android.model.Reservation;
import cn.android.utils.*;

import com.example.youzuo.R;

@SuppressLint({ "NewApi", "ShowToast" })
public class Main extends Activity {

	private static String query = "n";
	private ListView mListView;
	private SimpleAdapter adapter;
	private long mExitTime;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// action bar
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setTitle("");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.icon);
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionBarColor));

		// 处理查询
		handleIntent(getIntent());

		// drawer
		mListView = (ListView) findViewById(R.id.drawer_list);
		adapter = new SimpleAdapter(this, getData(),
				R.layout.drawer_list_style, new String[] { "itemTitle" },
				new int[] { R.id.itemTitle });
		adapter.notifyDataSetChanged();
		mListView.setAdapter(adapter);

		// 弹出键盘后屏幕不变
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

		// drawer 监听
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position == 0) {
					Intent toResInfo = new Intent();
					toResInfo.setClass(Main.this, MyReservation.class);
					startActivity(toResInfo);
					finish();
				}
			}
		});
		tableCreate();
	}

	@SuppressLint("NewApi")
	public static String getQuery() {
		return query;
	}

	public static void setQuery(String query) {
		Main.query = query;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {// 定义menu选项

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setSubmitButtonEnabled(true);
		searchView.setMinimumWidth((int) 20);

		return true;
	}

	private void handleIntent(Intent intent) {// 处理查询转到Reslist
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			ResList.restaurants.clear();
			// handles a search query
			Main.setQuery(intent.getStringExtra(SearchManager.QUERY));

			Intent toResList = new Intent();
			toResList.setClass(Main.this, ResList.class);
			startActivity(toResList);
			finish();
		}
	}

	private List<Map<String, Object>> getData() {// drawer数据获取
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("itemTitle", "我的预定");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("itemTitle", "我的收藏");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("itemTitle", "历史消费");
		list.add(map);

		return list;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {// 返回键监听
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();// 更新mExitTime
			} else {
				System.exit(0);// 否则退出程序
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void tableCreate() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.openOrCreateDatabase("reservation.db",
					Context.MODE_PRIVATE, null);
			String sql = "select count(*) as c from Sqlite_master  where type ='table'"
					+ " and name ='reservation' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count == 0) {
					db.execSQL("create if not exists table reservation(code TEXT NOT NULL, "
							+ "name TEXT NOT NULL,num INTEGER NOT NULL)");
				}
			}
			db.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
