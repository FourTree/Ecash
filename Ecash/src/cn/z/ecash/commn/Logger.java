package cn.z.ecash.commn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class Logger {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static String TAG = "CODEC";
	private static File logfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IcfccLog");
	private static String logname = "icfcc_"+dateFormat.format(new Date())+".log";
	private static File logFile = new File(logfile,logname);
	private static String fmt = "[%s] ";
	private static String df = "yyyy-MM-dd kk:mm:ss";
	private static FileWriter fw = null;
	private static BufferedWriter bw = null;
	private static StringWriter sw = null;
	private static PrintWriter pw = null;
	public static boolean log = true;
	public static boolean write = true;

	public Logger() {
		
	}
	
	static {
		try {
			if (write) {
				if(!logfile.exists()){
					if(!logfile.mkdir()){
						throw new Exception("目录不存在，创建失败！");
					}
				}
				if (!logFile.exists()) {
					logFile.createNewFile();
				}
				if (fw == null) {
					fw = new FileWriter(logFile, true);
				}
				if (bw == null) {
					bw = new BufferedWriter(fw);
				}
				File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Log.bin");
				if(file1.exists()){
					log = true;
				}
				file1 = null;
				File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Write.bin");
				if(file2.exists()){
					write = true;
				}
				file2 = null;
			}
		} catch (Exception e) {
			Log.e("LOGER", "初始化日志组建失败 ", e);
			write = false;
		}
	}

	public static void write(String logs) {
		String logString = String.format(fmt, DateFormat.format(df, System.currentTimeMillis()).toString()) + logs;
		try {
			bw.write(logString);
			bw.newLine();
			bw.flush();
			fw.flush();
		} catch (Exception e) {
			Log.e("LOGER", "write() ", e);
			write = false;
		}
	}

//	public static String getDate() {
//		return DateFormat.format(df, System.currentTimeMillis()).toString();
//	}

	public static void write(String log, Throwable throwable) {
		sw = new StringWriter();
		pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		String error = sw.getBuffer().toString();
		String logString = String.format(fmt, DateFormat.format(df, System.currentTimeMillis()).toString()) + log + "==>" + error;
		try {
			bw.write(logString);
			bw.newLine();
			bw.flush();
			fw.flush();
		} catch (Exception e) {
			Log.e("LOGER", "write() error", e);
			write = false;
		} finally {
			try {
				sw.close();
				pw.close();
			} catch (Exception e) {
				Log.e("LOGER", "write() error", e);
				write = false;
			}
			sw = null;
			pw = null;
		}
	}

	public static void i(String msg) {
		if (log) {
			Log.i(TAG, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void i(String tag, String msg) {
		if (log) {
			Log.i(tag, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void v(String msg) {
		if (log) {
			Log.v(TAG, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void v(String tag, String msg) {
		if (log) {
			Log.v(tag, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void d(String msg) {
		if (log) {
			Log.d(TAG, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void d(String tag, String msg) {
		if (log) {
			Log.d(tag, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void w(String msg) {
		if (log) {
			Log.w(TAG, msg);
		}
		if (write) {
			write(msg);
		}
	}
	
	public static void w(String tag, String msg) {
		if (log) {
			Log.w(tag, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void e(String msg) {
		if (log) {
			Log.e(TAG, msg);
		}
		if (write) {
			write(msg);
		}
	}
	
	public static void e(String tag, String msg) {
		if (log) {
			Log.e(tag, msg);
		}
		if (write) {
			write(msg);
		}
	}

	public static void e(String msg, Throwable e) {
		if (log) {
			Log.e(TAG, msg, e);
		}
		if (write) {
			write(msg, e);
		}
	}
	
	public static void e(String tag, String msg, Throwable e) {
		if (log) {
			Log.e(tag, msg, e);
		}
		if (write) {
			write(msg, e);
		}
	}

	//这个释放资源。若退出程序部在记录和打印日志则调用
	public static void close() {
		try {
			if (fw != null) {
				fw.close();
				fw = null;
			}
			if (bw != null) {
				bw.close();
				bw = null;
			}
			if (sw != null) {
				sw.close();
				sw = null;
			}
			if (pw != null) {
				pw.close();
				pw = null;
			}
		} catch (Exception e) {
			Log.e("LOGER", "close() error  ", e);
			write = false;
		}
	}
}