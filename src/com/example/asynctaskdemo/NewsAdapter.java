package com.example.asynctaskdemo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class NewsAdapter extends BaseAdapter implements OnScrollListener{

	private List<NewsBean> mList;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	private int mStart;
	private int mEnd;
	public static String[] URLS; 
	private boolean mFirstIn;
	
	public NewsAdapter(Context context,List<NewsBean> list,PullToRefreshListView mPullToRefreshListView){
		mList = list;
		mInflater = LayoutInflater.from(context);
		//��������Ŀ����Ϊ�˱���  
		//��new ImageLoader().showIamgeByAsyncTask(viewHolder.imageView, url);//ͨ���첽����ͼƬ��
		//ÿ�ζ�����һ��LruCache����
		mImageLoader = new ImageLoader(mPullToRefreshListView);
		URLS = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			URLS[i] = list.get(i).newsIconUrl;
		}
		mFirstIn = true;
		//ע���Ӧ���¼����мǣ�
		mPullToRefreshListView.setOnScrollListener(this);
	}
	
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_layout, null);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_image);
			viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
			viewHolder.content = (TextView) convertView.findViewById(R.id.tv_content);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		NewsBean bean = mList.get(position);
		String url = bean.newsIconUrl;
		viewHolder.imageView.setImageResource(R.drawable.ic_launcher);
		viewHolder.imageView.setTag(url);
//		new ImageLoader().showImageByThread(viewHolder.imageView, url); //ͨ�����̷߳�ʽ����ͼƬ
		mImageLoader.showIamgeByAsyncTask(viewHolder.imageView, url);//ͨ���첽����ͼƬ
		viewHolder.title.setText(bean.newstitle);
		viewHolder.content.setText(bean.newsContent);
		return convertView;
	}
	
	class ViewHolder{
		private ImageView imageView;
		private TextView title;
		private TextView content;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		mStart = firstVisibleItem;
		mEnd = firstVisibleItem + visibleItemCount;
		//��һ����ʾ��ʱ�����
		if (mFirstIn && visibleItemCount > 0) {
			mImageLoader.loadImages(mStart, mEnd);
			mFirstIn = false;
		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if (scrollState == SCROLL_STATE_IDLE) {
			//����������ͣ��״̬�����ؿɼ���
			mImageLoader.loadImages(mStart, mEnd);
		}else {
			//ͣ����������
			mImageLoader.cancelAllTasks();
		}
	}
	
}
