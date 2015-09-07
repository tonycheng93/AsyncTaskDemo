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
	// ����cache
	private LruCache<String, Bitmap> mCache;

	private PullToRefreshListView mListView;
	private Set<NewsAsyncTask> mTasks;

	public ImageLoader(PullToRefreshListView mPullToRefreshListView) {
		mListView = mPullToRefreshListView;
		mTasks = new HashSet<>();
		// ��ȡ�������ڴ�
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 4;
		mCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				// ��ÿ�δ��뻺��ĵ�ʱ�����
				return value.getByteCount();
			}
		};
	}

	// ���ӵ�����
	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (getBitmapFromCache(url) == null) {
			mCache.put(url, bitmap);
		}
	}

	// �ӻ����л�ȡ����
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
	 * ͨ�����̵߳ķ�ʽ����ͼƬ
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

	// �������ش�start��end������ͼƬ
	public void loadImages(int start, int end) {
		for (int i = start; i < end; i++) {
			String url = NewsAdapter.URLS[i];// ����ط�������������ˣ���δ���
			// �ӻ�����ȡ����Ӧ��ͼƬ
			Bitmap bitmap = getBitmapFromCache(url);
			// ���������û��ͼƬ����ô����ȥ����
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
	 * ͨ���첽�ķ�ʽ����ͼƬ
	 * 
	 * @param imageView
	 * @param url
	 */
	public void showIamgeByAsyncTask(ImageView imageView, String url) {
		// �ӻ�����ȡ����Ӧ��ͼƬ
		Bitmap bitmap = getBitmapFromCache(url);
		// ���������û��ͼƬ����ô����ȥ����
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
			// �������ȡͼƬ
			Bitmap bitmap = getBitmapFromURL(params[0]);
			if (bitmap != null) {
				// �����ٻ����ͼƬ��ŵ�����
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
