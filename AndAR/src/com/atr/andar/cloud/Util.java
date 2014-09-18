package com.atr.andar.cloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atr.andar.R;
import com.sina.sae.cloudservice.api.CloudClient;
import com.sina.sae.cloudservice.api.CloudDB;
import com.sina.sae.cloudservice.callback.ExecuteCallback;
import com.sina.sae.cloudservice.exception.CloudServiceException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Util {
	final static int INIT = 1;
	final static int FOOTER = 2;
	final static int HEADER = 3;
	final static int GETDATA = 4;
	final static int DELETEDATA = 5;
	final static int UPDOWN = 6;





	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	static int CROP_PHOTO_REQUEST_CODE = 1;

	static SharedPreferences preference;

	static int TESTLISTVIEW = 1;
	static int COMMENT = 2;

	/**
	 * Save Bitmap to a file.保存图片到SD卡。
	 * 
	 * @param bitmap
	 * @param file
	 * @return error message if the saving is failed. null if the saving is
	 *         successful.
	 * @throws IOException
	 */
	public static void saveBitmapToFile(Bitmap bitmap, String _file)
			throws IOException {
		BufferedOutputStream os = null;
		try {
			File file = new File(_file);
			// String _filePath_file.replace(File.separatorChar +
			// file.getName(), "");
			int end = _file.lastIndexOf(File.separator);
			String _filePath = _file.substring(0, end);
			File filePath = new File(_filePath);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			file.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static void do_not_close_dialog(DialogInterface dialog) {
		try {
			// 不关闭dialog
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void close_dialog(DialogInterface dialog) {
		// 关闭对话框
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dialog.dismiss();
	}

	static boolean isinlist(List<Integer> list, int i) {
		Iterator it1 = list.iterator();
		System.out.println("isinlist=====   " + list);
		while (it1.hasNext()) {
			if (i == Integer.parseInt(String.valueOf(it1.next()))) {
				System.out.println("在列表里");
				return true;
			}
		}
		System.out.println("不在列表里");
		return false;
	}

	static boolean isinlist(List<String> list, String i) {
		Iterator it1 = list.iterator();
		// System.out.println("isinlist=====   " + list);
		while (it1.hasNext()) {
			if (i.equals(String.valueOf(it1.next()))) {
				System.out.println("在列表里");
				return true;
			}
		}
		System.out.println("不在列表里");
		return false;
	}

	static int get_id_by_position(List<Map<String, String>> data, int position) {
		Map map = data.get(position);
		// Set set = map.entrySet();
		// Iterator iterator = set.iterator();
		// Map.Entry entry_sender = (Map.Entry) iterator.next();
		// Map.Entry entry_id = (Map.Entry) iterator.next();
		// int id = Integer.parseInt(entry_id.getValue().toString());
		int id = Integer.parseInt(map.get("id").toString());
		return id;
	}

	static int get_position_by_id(List<Map<String, String>> data, int id) {
		Map map;
		int result = -1;
		for (int i = 0; i < data.size() && result == -1; i++) {
			map = data.get(i);
			if (Integer.parseInt(map.get("id").toString()) == id) {
				result = i;
			}
		}
		return result;
	}

	// static String get_name_by_id(List<Map<String, String>> data, int id) {
	// Map map;
	// String result = "";
	// for (int i = 0; i < data.size() && result.equals(""); i++) {
	// map = data.get(i);
	// if (Integer.parseInt(map.get("id").toString()) == id) {
	// result = map.get("name").toString();
	// }
	// }
	// return result;
	// }

	static void init(final Context context, final String appname,
			final String ak, final String sk, final Handler initHandler,
			final int i) {
		new Thread() {
			public void run() {
				try {
					CloudClient.init(context, appname, ak, sk);
					switch (i) {
					case INIT:
						initHandler.obtainMessage(20).sendToTarget();
						break;
					case FOOTER:
						initHandler.obtainMessage(21).sendToTarget();
						break;
					case HEADER:
						initHandler.obtainMessage(22).sendToTarget();
						break;
					case GETDATA:
						initHandler.obtainMessage(20).sendToTarget();
						break;
					case DELETEDATA:
						initHandler.obtainMessage(21).sendToTarget();
						break;
					case UPDOWN:
						initHandler.obtainMessage(23).sendToTarget();
						break;
					}

					// Statistic.launch(TestListView.this);
					// Statistic.singleEvent("init Button");
					return;
				} catch (CloudServiceException localCloudServiceException) {
					switch (i) {
					case INIT:
						initHandler.obtainMessage(15).sendToTarget();
						break;
					case FOOTER:
						initHandler.obtainMessage(8).sendToTarget();
						break;
					case HEADER:
						initHandler.obtainMessage(9).sendToTarget();
						break;
					case GETDATA:
						initHandler.obtainMessage(2).sendToTarget();
						break;
					case DELETEDATA:
						initHandler.obtainMessage(14).sendToTarget();
						break;
					}
				}
			}
		}.start();
	}

	// 获取并设置ListView高度的方法
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1))
				+ 20;
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		listView.setLayoutParams(params);
	}

	

	// 根据生日获取年龄
	static int getAge(Date birthDay) throws Exception {
		Calendar cal = Calendar.getInstance();

		if (cal.before(birthDay)) {
			throw new IllegalArgumentException(
					"The birthDay is before Now.It's unbelievable!");
		}

		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH);
		int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
		cal.setTime(birthDay);

		int yearBirth = cal.get(Calendar.YEAR);
		int monthBirth = cal.get(Calendar.MONTH);
		int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

		int age = yearNow - yearBirth;

		if (monthNow <= monthBirth) {
			if (monthNow == monthBirth) {
				// monthNow==monthBirth
				if (dayOfMonthNow < dayOfMonthBirth) {
					age--;
				} else {
					// do nothing
				}
			} else {
				// monthNow>monthBirth
				age--;
			}
		} else {
			// monthNow<monthBirth
			// donothing
		}

		return age;
	}



	static void wipe_data(Context context) {
		preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preference.edit();
		editor.putString("myNickname", null);
		editor.putString("mySignature", null);
		editor.putString("myBirthday", null);
		editor.putString("mySex", null);
		editor.putString("myPlace", null);
		editor.putString("myPassword", null);
		editor.putString("myEmail", null);
		editor.putString("myDate", null);
		editor.commit();
	}

	static void save_data(Context context, String myNickname,
			String mySignature, String myBirthday, String mySex,
			String myPlace, String myPassword, String myEmail, String myDate) {
		preference = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preference.edit();
		editor.putString("myNickname", myNickname);
		editor.putString("mySignature", mySignature);
		editor.putString("myBirthday", myBirthday);
		editor.putString("mySex", mySex);
		editor.putString("myPlace", myPlace);
		editor.putString("myPassword", myPassword);
		editor.putString("myEmail", myEmail);
		editor.putString("myDate", myDate);
		editor.commit();
	}

	public static String get_path() {
		String avatarpath = "Android/data/com.atr.andar/model/";
		return avatarpath;
	}

	static String get_file_path(String avatar_name) {
		String avatarpath = get_path();
		String avatarfilepath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + avatarpath + avatar_name+".png";
		return avatarfilepath;
	}

	static String get_cloud_path(String avatar_name) {
//		CyEncoder c = new CyEncoder(avatar_name);
//		String uni = c.toUnicode('$');
//		System.out.println(uni);
		return "andar_model/" + avatar_name;
	}

	public static void cropPhoto(Activity act, String name) {
		File file = new File(get_file_path(name));

		FileUtils fileUtils = new FileUtils();
		fileUtils.creatSDDir(get_path());

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra("crop", "true");

		// aspectX aspectY 是图片宽高比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("output", Uri.fromFile(file));// 保存到文件
		intent.putExtra("outputFormat", "JPEG");// 返回格式
		intent.putExtra("noFaceDetection", true);
		act.startActivityForResult(intent, CROP_PHOTO_REQUEST_CODE);
	}

	/**
	 * 获取单个文件的MD5值！
	 * 
	 * @param file
	 * @return
	 */
	static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	static String email_display(String email) {
		if (email == null) {
			return null;
		} else {
			try {
				String[] s = new String[2];
				s = email.split("@");
				char s1 = s[0].charAt(0);
				String s2 = "";
				for (int i = 0; i < s[0].length() - 1; i++) {
					s2 = s2 + "*";
				}
				return s1 + s2 + "@" + s[1];
			} catch (Exception e) {
				// TODO: handle exception
				return email;
			}

		}
	}

	static void ShowProgressDialog(ProgressDialog mpDialog, String message) {
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mpDialog.setTitle("请稍等...");// 设置标题
		// mpDialog_register.setIcon(R.drawable.icon);//设置图标
		mpDialog.setMessage(message);
		mpDialog.setIndeterminate(true);// 设置进度条是否为不明确
		mpDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
		mpDialog.setCanceledOnTouchOutside(false);// 设置触摸屏幕外面的地方不消失
		mpDialog.show();
	}

	static void CancelProgressDialog(ProgressDialog mpDialog) {
		// if (mpDialog != null) {
		try {
			mpDialog.dismiss();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// }
	}

	static void MakeNotification(NotificationManager m_NotificationManager,
			Intent m_Intent, PendingIntent m_PendingIntent,
			int m_Notification_ID, Context context, String ticker,
			String title, String text) {
		// --构造通知栏通知begin--
		// 构造Notification对象 它就是一个通知消息对象
		// 我们只要知道我们要在手机最上边状态栏通知消息的时候就用这个对象
		Notification m_Notification = new Notification();

		// 设置通知在状态栏显示的图标
		m_Notification.icon = R.drawable.ic_launcher;
		// 当我们发送通知时在状态栏显示的内容
		m_Notification.tickerText = ticker;
		// 通知时震动
		m_Notification.defaults = Notification.DEFAULT_SOUND;
		// 设置通知显示的参数 (Context context, CharSequence
		// contentTitle,
		// CharSequence contentText, PendingIntent
		// contentIntent)
		// 参数1上下文对象Context 参数2 类似标题Title
		m_Notification
				.setLatestEventInfo(context, title, text, m_PendingIntent);
		// 让其无法被清楚
		// m_Notification.flags = Notification.FLAG_NO_CLEAR;
		// 可以理解为执行这个通知 或者说
		// 由NotificationManager对象或者它的一个引用把
		// 通知发出去然后就会在状态栏显示了

		m_NotificationManager.notify(m_Notification_ID, m_Notification);
		// --构造通知栏通知end--

	}

	static String get_key(String ori_key) {
		CyEncoder c = new CyEncoder(ori_key);
		String uni = c.toUnicode('_');
		System.out.println(uni);
		return "User" + uni;
	}

	

	public static String distance(float dis) {
		dis = dis / 1000;
		int i = (int) Math.round(dis * 10); // 小数点后 a 位前移，并四舍五入
		float f2 = (float) (i / (float) 10); // 还原小数点后 a 位
		if (f2 == 0.0) {
			return 0.1 + " 公里内";
		} else {
			return f2 + " 公里内";
		}
		// DecimalFormat df = new DecimalFormat("#.0");
		// String str = df.format(dis);
		// return str + " 公里以内";

	}
}
