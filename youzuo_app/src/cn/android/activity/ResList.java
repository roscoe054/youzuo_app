package cn.android.activity;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.android.model.Restaurant;
import cn.android.utils.MySuggestionProvider;
import cn.android.utils.StreamTool;

import com.example.youzuo.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("NewApi")
public class ResList extends Activity {
	private ListView mListView;
	private SimpleAdapter adapter;
	private TextView myKey;
	private TextView myResNum;
	private static int resId=-1;
	
//	public static String PATH = "http://192.168.1.101:8080/MyServerAppWeb/MyServlet";// 本地测试路径
	public static String PATH = "http://1.youzuo.sinaapp.com/MyServlet";// SAE路径
	
	public static List<Restaurant> restaurants = new ArrayList<Restaurant>();

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.res_list);

		// action bar
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setTitle("");
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setIcon(R.drawable.icon);
		actionBar.setDisplayHomeAsUpEnabled(true); 
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionBarColor));

		// init
		mListView = (ListView)findViewById(R.id.list);
		mListView.setContentDescription("no Thing");
		myKey=(TextView) findViewById(R.id.my_key);
		myResNum=(TextView) findViewById(R.id.my_res_num);
		
		//search
		showResults(Main.getQuery());

		// search suggestion
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
				MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
		suggestions.saveRecentQuery(Main.getQuery(), null);
		
		//处理查询
		handleIntent(getIntent());

		mListView.setOnItemClickListener(new OnItemClickListener() {//listview监听，为Resinfo准备数据
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				setResId(position);
				Intent toResInfo = new Intent();
				toResInfo.setClass(ResList.this,ResInfo.class);
				toResInfo.putExtra("name", restaurants.get(position).getName());
				toResInfo.putExtra("evaluation", restaurants.get(position).getEvaluation());
				toResInfo.putExtra("waitingNum", restaurants.get(position).getWaitingNum());
				toResInfo.putExtra("location", restaurants.get(position).getLocation());
				toResInfo.putExtra("tel", restaurants.get(position).getTel());
				toResInfo.putExtra("introduct", restaurants.get(position).getIntroduct());
				
				restaurants.clear();
				startActivity(toResInfo);
				finish();
			}
		});
		
	}
	
	public static int getResId(){
		return resId;
	}
	public void setResId(int id){
		resId=id;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){//左上返回按钮监听
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
        	Intent backResList = new Intent();
        	backResList.setClass(ResList.this,Main.class);
        	//backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(backResList);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {//菜单栏

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Associate searchable configuration with the SearchView 
		SearchManager searchManager = 
				(SearchManager) getSystemService(Context.SEARCH_SERVICE); 
		SearchView searchView = 
				(SearchView) menu.findItem(R.id.menu_search).getActionView(); 
		searchView.setSearchableInfo( 
				searchManager.getSearchableInfo(getComponentName()));
		searchView.setSubmitButtonEnabled(true);
		searchView.setMinimumWidth((int) 20);

		return true;
	}
	
	private void handleIntent(Intent intent) {//处理查询餐厅
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
			restaurants.clear();
            Main.setQuery(intent.getStringExtra(SearchManager.QUERY));
            
            //search
    		showResults(Main.getQuery());

    		// search suggestion
    		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
    				MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
    		suggestions.saveRecentQuery(Main.getQuery(), null);
        }
    }

	private void showResults(String query) {//获取结果更新adapter
		//缩短和显示搜索关键字
		String shortQuery=query;
		if(query.length()>5)
		{
			shortQuery=shortQuery.substring(0,4)+"...";
		}
		myKey.setText(shortQuery);
		
		List<Restaurant> Restaurants = searchSubmit(query);
		
		adapter = new SimpleAdapter(this, getData(Restaurants),
				R.layout.list_style, new String[] { 
			"name","des_evaluation", "evaluation","des_waitingNum","waitingNum"},
				new int[] { R.id.linestyle_name,R.id.des_evaluation,R.id.linestyle_evaluation,
			R.id.des_waitingNum,R.id.linestyle_waitingNum});
		adapter.notifyDataSetChanged();
		mListView.setAdapter(adapter);
	}

	private List<Map<String, Object>> getData(List<Restaurant> restaurants) {//获取各餐厅数据到listview
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.clear();
		int resNum=restaurants.size();
		myResNum.setText(" "+resNum+" ");
		for (Restaurant restaurant : restaurants) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", restaurant.getName());
			map.put("des_evaluation","评价 : ");
			map.put("evaluation", getStars(restaurant.getEvaluation()));
			map.put("des_waitingNum","等待人数 : ");
			map.put("waitingNum", restaurant.getWaitingNum());
			list.add(map);
		}
		return list;
	}

	@SuppressLint("ShowToast")
	private List<Restaurant> searchSubmit(String query) {//连接服务端体提交和返回数据
		try {
			String path = PATH + "?key=" + URLEncoder.encode(query, "UTF-8");
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
//				Toast.makeText(getApplicationContext(), "网络连接成功", 2000).show();
				byte[] data = StreamTool.readInputStream(conn.getInputStream());
				restaurants = getJSON(data);
				return restaurants;
			}
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), "网络连接失败", 2000).show();
			ex.printStackTrace();
		}
		return restaurants;
	}

	public static List<Restaurant> getJSON(byte[] data) throws JSONException {//解析返回的json
		String json = new String(data);
		JSONArray array = new JSONArray(json);
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.getJSONObject(i);
			String name = item.getString("name");
			int evaluation=item.getInt("evaluation");
			int waitingNum=item.getInt("waitingNum");
			String location = item.getString("location");
			String tel=item.getString("tel");
			String introduct=item.getString("introduct");
			restaurants.add(new Restaurant(name,evaluation,
					waitingNum,location,tel,introduct));
		}
		return restaurants;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {//返回键监听

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent backResList = new Intent();
			backResList.setClass(ResList.this, Main.class);
			backResList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(backResList);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private String getStars(int eva){
		String stars=null;
		switch(eva){
		case 0:break;
		case 1:stars="★";break;
		case 2:stars="★★";break;
		case 3:stars="★★★";break;
		case 4:stars="★★★★";break;
		case 5:stars="★★★★★";break;
		default:break;
		}
		return stars;
	}
}
