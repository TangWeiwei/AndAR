package com.atr.andar.cloud;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.atr.andar.Instructions;
import com.atr.andar.ModelViewer;
import com.atr.andar.R;
import com.atr.andar.cloud.ModelChooser.Item;
import com.atr.andar.cloud.ModelChooser.ModelChooserListAdapter;
import com.atr.andar.cloud.PullToRefreshView.OnFooterRefreshListener;
import com.atr.andar.cloud.PullToRefreshView.OnHeaderRefreshListener;
import com.sina.sae.cloudservice.api.CloudClient;
import com.sina.sae.cloudservice.api.CloudDB;
import com.sina.sae.cloudservice.api.CloudFile;
import com.sina.sae.cloudservice.callback.QueryCallback;
import com.sina.sae.cloudservice.exception.CloudServiceException;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class OnlineModels extends Activity implements OnHeaderRefreshListener,
		OnFooterRefreshListener {

	final String ak = "44m05kym55";
	final String sk = "h0l05l2k30mixk13jjy0ijz0lm3hx215lwyy53zm";
	final String appname = "lovetucao";
	public static boolean isInit = false;
	// public static List<Map<String, String>> datas = new ArrayList();
	public static int times = 1;
	public static int onetimerefresh = 10;
	private int HEAD = 1;
	private int FOOT = 2;
	private int head_or_foot = 0;
	public static boolean have_init_failed = false; // 已经尝试过初始化并且初始化失败

	private ProgressDialog waitDialog;
	PullToRefreshView mPullToRefreshView;
	private ListView listView;

	private static SharedPreferences preference;
	static SharedPreferences.Editor editor;
	SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd  hh:mm");
	String date = "";

	Vector<Item> models = new Vector<Item>();
	ModelChooserListAdapter da = new ModelChooserListAdapter(models);

	private Handler mHandler;
	private boolean mRunning = false;

	private String status_download = null;

	private List<String> list_download = new ArrayList<String>();
	private List<String> list_no_avatar = new ArrayList<String>();
	static LruCache<String, Bitmap> mMemoryCache;

	public Handler initHandler = new Handler() {
		public void handleMessage(Message paramMessage) {
			switch (paramMessage.what) {
			default:
			case 0:
				// Toast.makeText(getApplicationContext(), "init success!",
				// Toast.LENGTH_SHORT).show();

				// isInit = true;
				have_init_failed = false;
				Intent intent = new Intent("tucao_init_success");
				sendBroadcast(intent);

				System.out.println("初始化成功");
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "连接服务器失败，稍后再试",
						Toast.LENGTH_SHORT).show();
				dissmissdialog();
				System.out.println("连接服务器失败");
				have_init_failed = true;
				Intent intent1 = new Intent("tucao_init_failed");
				sendBroadcast(intent1);

				break;
			case 2:
				Toast.makeText(getApplicationContext(), "网络没连上？",
						Toast.LENGTH_SHORT).show();
				dissmissdialog();
				System.out.println("网络未连接");
				break;
			case 3:
				da.notifyDataSetChanged(); // 刚进来第一次读取数据成功
				System.out.println("notifyDataSetChanged");
				dissmissdialog();
				System.out.println("刚进来第一次读取数据成功");
				break;
			case 4:
				// Toast.makeText(getApplicationContext(), "发送成功,下拉刷新即可见",
				// Toast.LENGTH_SHORT).show();

				startdialog();
				times = 1;
				getdata(times, 2, "");
				break;
			case 5:
				Toast.makeText(getApplicationContext(), status_download,
						Toast.LENGTH_SHORT).show();
				break;
			case 6:
				da.notifyDataSetChanged();
				mPullToRefreshView.onFooterRefreshComplete();
				break;
			case 7:
				da.notifyDataSetChanged();
				date = sDateFormat.format(new java.util.Date());
				editor.putString("date", date);
				editor.commit();
				mPullToRefreshView.onHeaderRefreshComplete("更新于：" + date);
				// 发完新吐槽后会自动刷新，同事进度条会显示，之后自动去掉进度条
				dissmissdialog();
				break;
			case 8:
				System.out.println("t3");
				Toast.makeText(getApplicationContext(), "获取失败，重启应用就好啦",
						Toast.LENGTH_SHORT).show();
				System.out.println("t4");
				mPullToRefreshView.onFooterRefreshComplete();
				break;
			case 9:
				System.out.println("h3");
				Toast.makeText(getApplicationContext(), "刷新失败，重启应用就好啦",
						Toast.LENGTH_SHORT).show();
				System.out.println("h4");
				mPullToRefreshView.onHeaderRefreshComplete("更新于：" + date);

				// 发完新吐槽后会自动刷新，同事进度条会显示，之后自动去掉进度条
				dissmissdialog();
				break;
			case 10:
				Toast.makeText(getApplicationContext(), "网络没连上？",
						Toast.LENGTH_SHORT).show();
				System.out.println("网络未连接");
				mPullToRefreshView.onFooterRefreshComplete();
				break;
			case 11:
				Toast.makeText(getApplicationContext(), "网络没连上？",
						Toast.LENGTH_SHORT).show();
				System.out.println("网络未连接");
				mPullToRefreshView.onHeaderRefreshComplete("更新于：" + date);

				// 发完新吐槽后会自动刷新，同事进度条会显示，之后自动去掉进度条
				dissmissdialog();
				break;
			case 12:
				Toast.makeText(getApplicationContext(), "连接服务器失败",
						Toast.LENGTH_SHORT).show();
				System.out.println("连接服务器失败");
				mPullToRefreshView.onFooterRefreshComplete();
				break;
			case 13:
				Toast.makeText(getApplicationContext(), "连接服务器失败",
						Toast.LENGTH_SHORT).show();
				System.out.println("连接服务器失败");
				// date = sDateFormat.format(new java.util.Date());
				mPullToRefreshView.onHeaderRefreshComplete("更新于：" + date);

				// 发完新吐槽后会自动刷新，同事进度条会显示，之后自动去掉进度条
				dissmissdialog();
				break;
			case 14:
				break;
			case 15:
				// 应该是token失效
				Toast.makeText(getApplicationContext(), "超时未使用，重启应用就好啦",
						Toast.LENGTH_SHORT).show();
				dissmissdialog();
				break;
			case 16:
				System.out.println("收到初始化成功的广播，开始加载");
				if (head_or_foot == HEAD) {
					refresh();
				} else if (head_or_foot == FOOT) {
					getdata(times, 1, "");
				}
				break;
			case 17:
				System.out.println("收到初始化失败的广播");
				if (head_or_foot == HEAD) {
					mPullToRefreshView.onHeaderRefreshComplete("更新于：" + date);
				} else if (head_or_foot == FOOT) {
					mPullToRefreshView.onFooterRefreshComplete();
				}
				break;
			case 20:
				getdata(times, 0, "");
				break;
			case 21:
				getdata(times, 1, "");
				break;
			case 22:
				getdata(times, 2, "");
				break;
			case 23:
				break;
			case 24:
				break;
			case 25:
				da.notifyDataSetChanged();
				break;
			case 26:
				dissmissdialog();
				break;
			case 27:
				break;
			case 28:
				// 查询是否有未读私信
				// if (android.os.Build.VERSION.SDK_INT >= 11) {
				// invalidateOptionsMenu在API11下才能运行

				break;
			case 29:
				break;
			case 30:
				Toast.makeText(getApplicationContext(), "评论成功",
						Toast.LENGTH_SHORT).show();

				da.notifyDataSetChanged();
				break;
			case 31:
				// 应该是token失效
				Toast.makeText(getApplicationContext(), "评论失败，重启应用就好啦",
						Toast.LENGTH_SHORT).show();
				break;
			case 32:
				Toast.makeText(getApplicationContext(), "还没连上服务器呢，稍后再试",
						Toast.LENGTH_SHORT).show();
				break;
			case 33:
				Toast.makeText(getApplicationContext(), "网络没连上？",
						Toast.LENGTH_SHORT).show();
				break;
			case 34:
				// 查询是否有未读提醒

				break;
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_listview);
		// AssetManager am = getAssets();

		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.main_pull_refresh_view);
		listView = (ListView) findViewById(R.id.list);

		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);

		preference = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preference.edit();

		waitDialog = ProgressDialog.show(this, "",
				getResources().getText(R.string.loading), true);

		// Item item = new Item();
		// item.text = getResources().getString(R.string.choose_a_model);
		// item.type = Item.TYPE_HEADER;
		// models.add(item);

		listView.setAdapter(da);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				// Item item = models.get(position - 1);
				// String name = item.text;

				Item item = (Item) listView.getAdapter().getItem(position);
				String str = item.text;
			}
		});

		// sae
		if (CloudClient.checkNetwork(OnlineModels.this)) {
			init();
		} else {
			OnlineModels.this.initHandler.obtainMessage(2).sendToTarget();
		}

		HandlerThread thread = new HandlerThread("AvatarLoad");
		thread.start();// 创建一个HandlerThread并启动它
		mHandler = new Handler(thread.getLooper());// 使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
		mHandler.post(mBackgroundRunnable);// 将线程post到Handler中
		mRunning = true;
	}

	private void init() {
		new Thread() {
			public void run() {
				try {
					CloudClient.init(OnlineModels.this, appname, ak, sk);
					OnlineModels.this.initHandler.obtainMessage(0)
							.sendToTarget();
					isInit = true;
					getdata(times, 0, "");

					// Statistic.launch(TestListView.this);
					// Statistic.singleEvent("init Button");
					return;
				} catch (CloudServiceException localCloudServiceException) {
					// 以防转换了服务器地址

				}
			}
		}.start();
	}

	void getdata(final int time, final int i, final String s) {
		// time判断第几次加载,i判断是刚进入还是head还是foot，j判断是普通还是搜索，s传入搜索值
		if (CloudClient.checkNetwork(OnlineModels.this)) {
			if (isInit) {
				try {
					// CloudDB.execute(str);
					String sql = null;
					sql = "select * from andar order by id desc limit "
							+ (time * onetimerefresh - onetimerefresh) + ","
							+ onetimerefresh;

					System.out.println(sql);
					CloudDB.query(sql, new QueryCallback() {
						@Override
						// 如果e为null取出list中的值..
						public void handle(List<Map<String, String>> list,
								CloudServiceException e) {
							if (e == null) {
								System.out.println("成功获得数据,times=" + time);

								try {
									if (i == 2) {
										models.clear();
									}
									for (int m = 0; m < list.size(); m++) {
										Item item = new Item();
										item.text = list.get(m).get("name");
										models.add(item);
										// datas.add(list.get(i));
										// System.out.println(list.get(i));
									}
									// System.out.println(datas);
								} catch (Exception e2) {
									// TODO: handle exception
									e2.printStackTrace();
								} finally {
									if (i == 0) { // 刚进来的getdata
										OnlineModels.this.initHandler
												.obtainMessage(3)
												.sendToTarget();
									} else if (i == 1) { // onFooterRefresh的getdata
										OnlineModels.this.initHandler
												.obtainMessage(6)
												.sendToTarget();

									} else if (i == 2) { // onHeaderRefresh的getdata
										OnlineModels.this.initHandler
												.obtainMessage(7)
												.sendToTarget();
									}
								}

							} else {
								System.out.println(e);
								System.out.println("===1===");
								// 超时未连接导致token失效
								// init();
								/*
								 * if (e.getCode() == 1001) { // Check Token
								 * Fail System.out.println("Check Token Fail");
								 * TestListView.this.initHandler
								 * .obtainMessage(15).sendToTarget(); }
								 */
								if (i == 0) {
									System.out
											.println("服务器连上了但是请求数据失败，一般是因为token失效");

									Util.init(OnlineModels.this, appname, ak,
											sk, initHandler, Util.INIT);

									// TestListView.this.initHandler
									// .obtainMessage(15).sendToTarget();
								} else if (i == 1) {
									System.out.println("t2");
									// TestListView.this.initHandler
									// .obtainMessage(8).sendToTarget();
									Util.init(OnlineModels.this, appname, ak,
											sk, initHandler, Util.FOOTER);

								} else if (i == 2) {
									System.out.println("h2");
									// TestListView.this.initHandler
									// .obtainMessage(9).sendToTarget();
									Util.init(OnlineModels.this, appname, ak,
											sk, initHandler, Util.HEADER);

								}

							}
						}
					});
					// da.notifyDataSetChanged();
				} catch (CloudServiceException localCloudServiceException) {
					// dialog(localCloudServiceException.getMessage());
					if (i == 1) {
						System.out.println("t2");
						OnlineModels.this.initHandler.obtainMessage(8)
								.sendToTarget();
					} else if (i == 2) {
						System.out.println("h2");
						OnlineModels.this.initHandler.obtainMessage(9)
								.sendToTarget();
					}
				}
			} else {
				if (i == 1) {
					if (have_init_failed) {
						OnlineModels.this.initHandler.obtainMessage(12)
								.sendToTarget();
					} else {
						head_or_foot = HEAD;
					}
				} else if (i == 2 || i == 0) {
					if (have_init_failed) {
						OnlineModels.this.initHandler.obtainMessage(13)
								.sendToTarget();
					} else {
						head_or_foot = FOOT;
					}
				}
			}

		} else {
			if (i == 1) {
				OnlineModels.this.initHandler.obtainMessage(10).sendToTarget();
			} else if (i == 2 || i == 0) {
				OnlineModels.this.initHandler.obtainMessage(11).sendToTarget();
			}

		}

	}

	void refresh() {
		times = 1;

		// da.notifyDataSetChanged();
		getdata(times, 2, "");
	}

	private void startdialog() {
		if (!waitDialog.isShowing()) {
			waitDialog.show();
		}

	}

	private void dissmissdialog() {
		if (waitDialog.isShowing()) {
			waitDialog.dismiss();
		}
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		System.out.println("onFooterRefresh");

		times = times + 1;
		da.notifyDataSetChanged();
		getdata(times, 1, "");
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		System.out.println("onHeaderRefresh");

		refresh();
	}

	class ModelChooserListAdapter extends BaseAdapter {

		private Vector<Item> items;

		public ModelChooserListAdapter(Vector<Item> items) {
			this.items = items;

			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

			// Use 1/8th of the available memory for this memory cache.
			final int cacheSize = maxMemory;
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					// The cache size will be measured in kilobytes rather than
					// number of items.
					return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
				}
			};
		}

		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			// normal items, and the header
			return 2;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return !(items.get(position).type == Item.TYPE_HEADER);
		}

		@Override
		public int getItemViewType(int position) {
			return items.get(position).type;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			final Item item = items.get(position);
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				switch (item.type) {
				case Item.TYPE_HEADER:
					v = vi.inflate(R.layout.list_header, null);
					break;
				case Item.TYPE_ITEM:
					v = vi.inflate(R.layout.choose_model_row_online, null);
					break;
				}
			}
			if (item != null) {
				// switch (item.type) {
				// case Item.TYPE_HEADER:
				// TextView headerText = (TextView) v
				// .findViewById(R.id.list_header_title);
				// if (headerText != null) {
				// headerText.setText(item.text);
				// }
				// break;
				// case Item.TYPE_ITEM:
				// Object iconImage = item.icon;
				ImageView icon = (ImageView) v
						.findViewById(R.id.choose_model_row_icon);
				// if (icon != null) {
				// if (iconImage instanceof Integer) {
				// icon.setImageResource(((Integer) iconImage)
				// .intValue());
				// } else if (iconImage instanceof Bitmap) {
				// icon.setImageBitmap((Bitmap) iconImage);
				// }
				// }

				// 设置头像
				String avatarfilepath = Util.get_file_path(item.text);

				try {
					Bitmap bitmap = getBitmapFromMemCache(item.text);
					if (bitmap == null) { // 不在map里
						if (!Util.isinlist(list_no_avatar, item.text)) {
							// 若已知该用户没有头像就不加载了
							BitmapFactory.Options option = new BitmapFactory.Options();
							try {
								bitmap = BitmapFactory.decodeFile(
										avatarfilepath, option);
								addBitmapToMemoryCache(item.text, bitmap); // 不在map里但是在sd卡上
								icon.setImageBitmap(bitmap);
							} catch (Exception e) {
								// TODO: handle exception
								// 不在map里也不在sd卡上

								if (!Util.isinlist(list_download, item.text)) {
									list_download.add(item.text);
								}

							}
						}
					} else {
						// 在map里面
						icon.setImageBitmap(bitmap);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				TextView text = (TextView) v
						.findViewById(R.id.choose_model_row_text);
				if (text != null)
					text.setText(item.text);
				// break;
				// }

				final LinearLayout download = (LinearLayout) v
						.findViewById(R.id.download);
				final TextView download_text = (TextView) v
						.findViewById(R.id.download_text);
				if (ModelChooser.list_name.contains(item.text)) {
					download.setEnabled(false);
					download.setBackgroundResource(R.drawable.mm_btn_grey_press);
					download_text.setText(v.getResources().getString(
							R.string.have_download));
				}

				download.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							download.setBackgroundResource(R.drawable.mm_btn_grey_press);
							break;
						case MotionEvent.ACTION_CANCEL:
							download.setBackgroundResource(R.drawable.mm_btn_grey_normal);
							break;
						case MotionEvent.ACTION_UP:
							download.setBackgroundResource(R.drawable.mm_btn_grey_normal);
							break;						
						}
						return false;
					}
				});

				download.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						// TODO Auto-generated method stub
						status_download = v.getResources().getString(
								R.string.download_start);
						OnlineModels.this.initHandler.obtainMessage(5)
								.sendToTarget();
						download_text.setText(v.getResources().getString(
								R.string.download_ing));

						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								String name = item.text;

								// if (!Util.isinlist(list_no_need_download,
								// name))
								// {
								String avatarpath = Util.get_path();
								// if (android.os.Build.VERSION.SDK_INT >=
								// 19) {
								// avatarpath =
								// "Android/data/com.adam.tucao/avatar/";
								// } else {
								// avatarpath = "adamsoft/tucao/avatar/";
								// }

								System.out.println("name == " + name);
								System.out
										.println("avatapath == " + avatarpath);
								String webpath = null;
								for (int i = 0; i < 3; i++) {
									switch (i) {
									case 0:
										webpath = Util.get_cloud_path(name)
												+ ".obj";
										System.out.println("webpath == "
												+ webpath);
										break;
									case 1:
										webpath = Util.get_cloud_path(name)
												+ ".mtl";
										System.out.println("webpath == "
												+ webpath);
										break;
									case 2:
										webpath = Util.get_cloud_path(name)
												+ ".png";
										System.out.println("webpath == "
												+ webpath);
										break;
									}

									try {
										CloudFile file = CloudFile
												.fetch(webpath);
										String url = file.getUrl();// 通过这个url可以直接访问云端文件
										System.out.println("url == " + url);
										HttpDownloader httpDownLoader = new HttpDownloader();
										int result = -1;
										switch (i) {
										case 0:
											result = httpDownLoader.downfile(
													url, avatarpath, name
															+ ".obj");
											break;
										case 1:
											result = httpDownLoader.downfile(
													url, avatarpath, name
															+ ".mtl");
											break;
										case 2:
											result = httpDownLoader.downfile(
													url, avatarpath, name
															+ ".png");
											break;
										}

										if (result == 0 || result == 1) { // 0成功，1文件已存在
											// Toast.makeText(getApplicationContext(),
											// "下载成功！",
											// Toast.LENGTH_SHORT).show();
											System.out.println("下载成功");
											if (i == 0) {
												ModelChooser.list_name
														.add(name);
											}

										} else if (result == -1) {
											// Toast.makeText(getApplicationContext(),
											// "下载失败！",
											// Toast.LENGTH_SHORT).show();
											System.out.println("下载失败");
											// 视为该用户没有头像
										}
										// list_no_need_download.add(name);
										// } else {
										// System.out.println("无需下载");
										// }

									} catch (CloudServiceException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println("下载失败");
										// 视为该用户没有头像
										// list_no_need_download.add(name);
									} finally {
										if (i == 2) {
											status_download = v
													.getResources()
													.getString(
															R.string.download_finish);
											OnlineModels.this.initHandler
													.obtainMessage(5)
													.sendToTarget();

											OnlineModels.this.initHandler
													.obtainMessage(25)
													.sendToTarget();
										}
									}
								}

							}
						}).start();
						;
					}
				});
			}
			return v;
		}

		private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
			if (getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}

		private Bitmap getBitmapFromMemCache(String key) {
			return mMemoryCache.get(key);
		}

	}

	// 实现耗时操作的线程
	Runnable mBackgroundRunnable = new Runnable() {

		@Override
		public void run() {
			// ----------耗时的操作，开始---------------
			while (true) {
				// Log.i("TestListView", "thread running!");
				if (mRunning) {
					String name = null;
					if (list_download.size() != 0) {
						try {
							name = list_download.get(0);
							// if (!Util.isinlist(list_no_need_download, name))
							// {
							String avatarpath = Util.get_path();
							// if (android.os.Build.VERSION.SDK_INT >= 19) {
							// avatarpath =
							// "Android/data/com.adam.tucao/avatar/";
							// } else {
							// avatarpath = "adamsoft/tucao/avatar/";
							// }

							System.out.println("name == " + name);
							System.out.println("avatapath == " + avatarpath);
							String webpath = Util.get_cloud_path(name) + ".png";
							System.out.println("webpath == " + webpath);
							CloudFile file = CloudFile.fetch(webpath);
							String url = file.getUrl();// 通过这个url可以直接访问云端文件
							System.out.println("url == " + url);
							HttpDownloader httpDownLoader = new HttpDownloader();
							int result = httpDownLoader.downfile(url,
									avatarpath, name + ".png");
							if (result == 0 || result == 1) { // 0成功，1文件已存在
								// Toast.makeText(getApplicationContext(),
								// "下载成功！",
								// Toast.LENGTH_SHORT).show();
								System.out.println("下载成功");
								OnlineModels.this.initHandler.obtainMessage(25)
										.sendToTarget();
							} else if (result == -1) {
								// Toast.makeText(getApplicationContext(),
								// "下载失败！",
								// Toast.LENGTH_SHORT).show();
								System.out.println("下载失败");
								// 视为该用户没有头像
								list_no_avatar.add(name);
							}
							list_download.remove(name);
							// list_no_need_download.add(name);
							// } else {
							// System.out.println("无需下载");
							// }

						} catch (CloudServiceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println("下载失败");
							// 视为该用户没有头像
							list_no_avatar.add(name);
							list_download.remove(name);
							// list_no_need_download.add(name);
						}
					}
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// ----------耗时的操作，结束---------------
		}
	};

	class Item {
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_HEADER = 1;
		private int type = TYPE_ITEM;
		private Object icon = new Integer(R.drawable.missingimage);
		private String text;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		times = 1;

		// 销毁线程
		mHandler.removeCallbacks(mBackgroundRunnable);
		mMemoryCache.evictAll();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mRunning = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mRunning = true;
		super.onResume();
	}

}
