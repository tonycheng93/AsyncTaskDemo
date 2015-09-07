package com.example.asynctaskdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private PullToRefreshListView mPullToRefreshListView;
	private final static String URL = "http://www.imooc.com/api/teacher?type=4&num=30";

	// private final static int PAGECOUNT = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		new MyAsyncTask().execute(URL);
	}

	/**
	 * 实现网络的异步访问
	 */
	class MyAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

		@Override
		protected List<NewsBean> doInBackground(String... params) {
			// TODO Auto-generated method stub
			return getJsonData(params[0]);
		}

		@Override
		protected void onPostExecute(List<NewsBean> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			NewsAdapter mAdapter = new NewsAdapter(MainActivity.this, result,
					mPullToRefreshListView);
			mPullToRefreshListView.setAdapter(mAdapter);
			mPullToRefreshListView.setMode(Mode.BOTH);
		}
	}

	/**
	 * 将url对应的json格式的数据转化为我们封装的NewsBean对象
	 * 
	 * @param url
	 * @return
	 */
	public List<NewsBean> getJsonData(String url) {
		// TODO Auto-generated method stub
		List<NewsBean> newsBeanList = new ArrayList<>();
		try {
			String jsonString = readStream(new URL(url).openStream());
			Log.d("json", jsonString);
			JSONObject jsonObject;
			NewsBean newsBean;
			try {
				jsonObject = new JSONObject(jsonString);
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					jsonObject = jsonArray.getJSONObject(i);
					newsBean = new NewsBean();
					newsBean.newsIconUrl = jsonObject.getString("picSmall");
					newsBean.newstitle = jsonObject.getString("name");
					newsBean.newsContent = jsonObject.getString("description");
					newsBeanList.add(newsBean);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newsBeanList;
	}

	/**
	 * 通过is解析网页返回的数组
	 * 
	 * @param is
	 * @return
	 */
	private String readStream(InputStream is) {
		InputStreamReader isr;
		String result = "";
		try {
			String line = "";
			isr = new InputStreamReader(is, "utf-8");
			BufferedReader reader = new BufferedReader(isr);
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		/**
		 * 实现下拉刷新和上拉加载更多
		 */
		if (view == mPullToRefreshListView) {
			mPullToRefreshListView
					.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener2<ListView>() {

						@Override
						public void onPullDownToRefresh(
								PullToRefreshBase<ListView> refreshView) {
							// TODO Auto-generated method stub
							new MyAsyncTask().execute(URL);
							mPullToRefreshListView.onRefreshComplete();
						}

						@Override
						public void onPullUpToRefresh(
								PullToRefreshBase<ListView> refreshView) {
							// TODO Auto-generated method stub
							new MyAsyncTask().execute(URL);
							mPullToRefreshListView.onRefreshComplete();
						}
					});
		}
	}
}
