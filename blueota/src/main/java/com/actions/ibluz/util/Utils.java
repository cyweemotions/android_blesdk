package com.actions.ibluz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Environment;
import com.tencent.mars.xlog.Log;

public class Utils {
	public static final boolean DEBUG_SPP = false;
	//Test review
	private static Context sContext = null;

	public static final String ISDIALOG_SHOWING = "is_dialog_showing";
	public static final String DIALOG_SHOWING = "dialog_showing";

	public static boolean[] checkExternalStorageAvailable() {
		boolean mExternalStorageReadable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageReadable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageReadable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageReadable = mExternalStorageWriteable = false;
		}
		boolean[] rwstate = { mExternalStorageReadable, mExternalStorageWriteable };
		return rwstate;
	}

	public static void createExternalStoragePrivateFile(Context context, String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(context.getExternalFilesDir(null), filename);
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				os.write(buffer);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	public static boolean hasExternalStoragePrivateFile(Context context, String filename) {
		if (checkExternalStorageAvailable()[0]) {
			File file = new File(context.getExternalFilesDir(null), filename);
			return file.exists();
		} else {
			Log.w("ExternalStorage", "Error reading");
			return false;
		}
	}

	public static void setContext(Context context) {
		sContext = context;
	}

	public static void createExternalStoragePrivateFile(String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(sContext.getExternalFilesDir(null), filename);
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				if (buffer != null) {
					os.write(buffer);
				}
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	public static void appendExternalStoragePrivateFile(String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(sContext.getExternalFilesDir(null), filename);
			OutputStream os = null;
			try {
				os = new FileOutputStream(file, true);
				os.write(buffer);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	public static boolean isAppForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null || appProcesses.size() == 0) {
			return false;
		}

		for (RunningAppProcessInfo process : appProcesses) {
			if (process.processName.equals(context.getPackageName())) {
				if (process.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				}
			}
		}

		return false;
	}

	public static String hexBuffer2String(byte[] buffer) {
		String hexStr = "";
		for(int x = 0; x < buffer.length; x++){
			String temp = Integer.toHexString(buffer[x] & 0xFF);
			if(temp.length() == 1){
				temp = "0" + temp;
			}
			hexStr = hexStr + " " + temp;
		}
		return hexStr;
	}

	public static void printHexBuffer(String tag, byte[] buffer) {
		// 打印Buffer
		String printHexStr = hexBuffer2String(buffer);
		Log.v(tag + "nickk", printHexStr);
	}

	public static int byteToInt(byte b) {
		return b & 0xFF;
	}

	public static int byteArrayToInt(byte[] b, int offset) {
		return 	 b[offset++] & 0xFF |
				(b[offset++] & 0xFF) << 8 |
				(b[offset++] & 0xFF) << 16 |
				(b[offset++] & 0xFF) << 24;
	}

	public static int byteArrayToShort(byte[] b, int offset) {
		return   b[offset++] & 0xFF |
				(b[offset++] & 0xFF) << 8;
	}
}
