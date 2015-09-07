package com.example.asynctaskdemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ImageLoader {

	private ImageView mImageView;
	private String mUrl;
	// 创建cache
	private LruCache<String, Bitmap> mCache;

	private PullToRefreshListView mListView;
	private Set<NewsAsyncTask> mTasks;

	public ImageLoader(PullToRefreshListView mPullToRefreshListView) {
		mListView = mPullToRefreshListView;
		mTasks = new HashSet<>();
		// 获取最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 4;
		mCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				// 在每次存入缓存的的时候调用
				return value.getByteCount();
			}
		};
	}

	// 增加到缓存
	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (getBitmapFromCache(url) == null) {
			mCache.put(url, bitmap);
		}
	}

	// 从缓存中获取数据
	public Bitmap getBitmapFromCache(String url) {
		return mCache.get(url);

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mImageView.getTag().equals(mUrl)) {
				mImageView.setImageBitmap((Bitmap) msg.obj);
			}
		}
	};

	/**
	 * 通过多线程的方式加载图片
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showImageByThread(ImageView imageView, final String url) {
		mImageView = imageView;
		mUrl = url;
		new Thread(new Runnable() {

			@Override
			public void run() {
				Bitmap bitmap = getBitmapFromURL(url);
				Message message = Message.obtain();
				message.obj = bitmap;
				mHandler.sendMessage(message);
			}
		}).start();
	}

	public Bitmap getBitmapFromURL(String urlString) {
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			URL url = new URL(urlString);
			try {
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				is = new BufferedInputStream(connection.getInputStream());
				bitmap = BitmapFactory.decodeStream(is);
				connection.disconnect();
				// return bitmap;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// return null;
		return bitmap;
	}

	public void cancelAllTasks() {
		// TODO Auto-generated method stub
		if (mTasks != null) {
			for (NewsAsyncTask task : mTasks) {
				task.cancel(false);
			}
		}
	}

	// 用来加载从start到end的所有图片
	public void loadImages(int start, int end) {
		for (int i = start; i < end; i++) {
			String url = NewsAdapter.URLS[i];// 这个地方数组索引溢出了，还未解决
			// 从缓存中取出对应的图片
			Bitmap bitmap = getBitmapFromCache(url);
			// 如果缓存中没有图片，那么必须去下载
			if (bitmap == null) {
				NewsAsyncTask task = new NewsAsyncTask(url);
				task.execute(url);
				mTasks.add(task);
			} else {
				ImageView imageView = (ImageView) mListView
						.findViewWithTag(url);
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	/**
	 * 通过异步的方式加载图片
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showIamgeByAsyncTask(ImageView imageView, String url) {
		// 从缓存中取出对应的图片
		Bitmap bitmap = getBitmapFromCache(url);
		// 如果缓存中没有图片，那么必须去下载
		if (bitmap == null) {
			imageView.setImageResource(R.drawable.ic_launcher);
		} else {
			imageView.setImageBitmap(bitmap);
		}

	}

	private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

		// private ImageView mImageView;
		private String mUrl;

		public NewsAsyncTask(String url) {
			mUrl = url;
			// mImageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			// 从网络获取图片
			Bitmap bitmap = getBitmapFromURL(params[0]);
			if (bitmap != null) {
				// 将不再缓存的图片存放到缓存
				addBitmapToCache(url, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// TODO Auto-generated method stub
			super.onPostExecute(bitmap);
			// if (mImageView.getTag().equals(mUrl)) {
			// mImageView.setImageBitmap(bitmap);
			// }
			ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			mTasks.remove(this);
		}
	}

}
