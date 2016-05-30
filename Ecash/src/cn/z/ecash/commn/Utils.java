//$Id: Utils.java,v 1.1.1.1 2007/10/06 13:47:03 benmoez Exp $

/**
 * Author : Moez Ben MBarka Moez
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



// $Id: Utils.java,v 1.1.1.1 2007/10/06 13:47:03 benmoez Exp $

package cn.z.ecash.commn;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random ;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


/**
 * Some util methods. 
 *
 * @author Moez Ben MBarka
 * @version $Revision: 1.1.1.1 $
 */
public class Utils {
	private static String LOG_DIR = "log";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
    private static final char[] HEX_DIGITS =
    {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    private static int fromDigit(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        if (ch >= 'A' && ch <= 'F')
            return ch - 'A' + 10;
        if (ch >= 'a' && ch <= 'f')
            return ch - 'a' + 10;
        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }
    
    /**
     * Returns a hex string representing the byte array.
     * @param ba The byte array to hexify.
     * @return The hex string.
     */
    public static String toHexString( byte[] ba ) {
        int length = ba.length;
        char[] buf = new char[length * 3];
        for (int i = 0, j = 0, k; i < length; ) {
            k = ba[i++];
            buf[j++] = HEX_DIGITS[(k >> 4) & 0x0F];
            buf[j++] = HEX_DIGITS[ k       & 0x0F];
            buf[j++] = ' ';
        }
        return new String(buf, 0, buf.length-1);
    }
    
    public static String toHexStringNoBlank( byte[] ba ) {
    	if(null == ba)
    		return null;
        int length = ba.length;
        char[] buf = new char[length * 2];
        for (int i = 0, j = 0, k; i < length; ) {
            k = ba[i++];
            buf[j++] = HEX_DIGITS[(k >> 4) & 0x0F];
            buf[j++] = HEX_DIGITS[ k       & 0x0F];
        }
        return new String(buf, 0, buf.length);
    }
    
    public static byte[] bytesFromHexString(String hex) throws NumberFormatException  {
        if (null == hex || hex.length() == 0) return null;
        String myhex = hex + " ";
        int len = myhex.length();
        if ((len % 3) != 0) throw new NumberFormatException();
        byte[] buf = new byte[len / 3];
        int i = 0, j = 0;
        while (i < len) {
            try {
                buf[j++] = (byte) ((fromDigit(myhex.charAt(i++)) << 4) |
                        fromDigit(myhex.charAt(i++)));
            } catch (IllegalArgumentException e) {
                throw new NumberFormatException();
            }
            if (myhex.charAt(i++) != ' ') throw new NumberFormatException();
        }
        return buf;
    }
    
    public static byte[] bytesFromHexStringNoBlank(String hex) throws NumberFormatException  {
        if (null == hex || hex.length() == 0) return null;
        String myhex = hex;
        int len = myhex.length();
        if ((len % 2) != 0) throw new NumberFormatException();
        byte[] buf = new byte[len / 2];
        int i = 0, j = 0;
        while (i < len) {
            try {
                buf[j++] = (byte) ((fromDigit(myhex.charAt(i++)) << 4) |
                        fromDigit(myhex.charAt(i++)));
            } catch (IllegalArgumentException e) {
                throw new NumberFormatException();
            }
        }
        return buf;
    }
    
    public static byte[] buildHeader (byte cla,byte ins,byte p1,byte p2,byte lc){
        byte[] header={cla,ins,p1,p2,lc};
        return header;
    }

    public static byte[] rand_bytes(int size){
		Random rand =new Random() ;
		byte[] result  = new byte[size] ;
		rand.nextBytes(result) ;
		return result ;
	}
    
    public static byte[] clone_array(byte[] src){
    	byte[] dest = new byte[src.length] ;
    	System.arraycopy(src, 0, dest,0, src.length) ;
    	return dest;
    }
    
    public static byte[] SHA1(byte[] data) throws NoSuchAlgorithmException{
    	
    	MessageDigest md = null;
		md = MessageDigest.getInstance("SHA-1");
    	md.update(data);
    	return md.digest();
    }
    
    public static byte[] getBERLen(int len){
    	
    	byte[] len_b = null;
    	
    	if(len >= 0x100){
    		len_b = new byte[3];
    		len_b[0] = (byte)0x82;
    		len_b[1] = (byte)(len >> 8);
    		len_b[2] = (byte)(len & 0xFF);
    	}else if(len >= 0x80){
    		len_b = new byte[2];
    		len_b[0] = (byte)0x81;
    		len_b[1] = (byte)len;
    	}else{
    		len_b = new byte[]{(byte)len};
    	}
    	
    	return len_b;
    }
    
    /**
     * 解析TLV标签
     * @param str
     * @return
     */
	public static List<TLVEntity> getNodes(String str) {
		byte[] datB = str.getBytes();
		byte[] temp = new byte[datB.length / 2];

		AsciiToBcd(temp, 0, datB, 0, datB.length, 0);

		ArrayList<TLVEntity> tlvEntity = null;

		try {
			tlvEntity = TLVEntity.unpacket_entity(temp, 0, temp.length - 2);
		} catch (TLVParserException e) {
			Logger.e("getNodes", "解析TLV出错", e);
			tlvEntity = null;
		}

		return tlvEntity.get(0).getNodes();
	}

	public static void AsciiToBcd(byte[] bcd_buf, int bcd_offset,
			byte[] ascii_buf, int ascii_offset, int conv_len, int type) {
		int cnt;
		int ch, ch1;

		if ((0x01 == (conv_len & 0x01)) && type == 1) /* 判别是否为奇数以及往那边对齐 */
			ch1 = 0;
		else
			ch1 = 0x55;

		for (cnt = 0; cnt < conv_len; ascii_offset++, cnt++) {
			int tmpascii = (ascii_buf[ascii_offset] & 0xFF);
			if (tmpascii >= 'a')
				ch = (tmpascii - 'a' + 10);
			else if (tmpascii >= 'A')
				ch = (tmpascii - 'A' + 10);
			else if (ascii_buf[ascii_offset] >= '0')
				ch = (tmpascii - '0');
			else {

				ch = tmpascii;
				ch &= 0x0f;// 保留低四�?
			}
			if (ch1 == 0x55)
				ch1 = ch;
			else {
				bcd_buf[bcd_offset] = (byte) ((ch1 << 4) | ch);
				bcd_offset++;
				ch1 = 0x55;
			}
		} // for

		if (ch1 != 0x55)
			bcd_buf[bcd_offset] = (byte) (ch1 << 4);

		return;
	}
	
//    /**
//     * 
//     * 方法描述：获取上传的日志文件
//     * @return
//     */
//    public static ArrayList<String> getLogPathList()
//    {
//    	ArrayList<String> filePath = new ArrayList<String>();
//		//获取所有错误日志文件
//		filePath.addAll(getLogList());
//		filePath.addAll(getAppLogList());
//		Log.d("logFileNum", filePath.size() + " 个log文件");
//        return filePath;
//    }
    
//    // 获取app错误日志
//    public static ArrayList<String> getAppLogList() {
//		// 保证上传两个大于10K的日志文件。
//		// 例外，本身没有大于10K的日志。
//		ArrayList<String> filePath = new ArrayList<String>(); 
//		// 获取插件日志
//		String getAppLogDir = getAppLogDir();
//		if (!TextUtils.isEmpty(getAppLogDir)) {
//			File[] files = new File(getAppLogDir).listFiles();
//			boolean boo=true;
//			int length=files.length - 1;
//			while(boo){
//				if (length > -1 && files[length].getName().contains(".log") && !files[length].getName().endsWith(".zip")){
//					
//					try{
//		            	if(files[length].isFile() && files[length].length()>Math.pow(10, 4)){//Math.pow(10, 4)  为10000，即伪10K，实际上不到10K了，你懂的
//		                    String zipPath = files[length].getAbsolutePath()+".zip";
//		                    ZipUtil.compress(files[length].getAbsolutePath(), zipPath);
//		                    filePath.add(zipPath);
//		            	}
//		            }catch (IOException e){
//		                Logger.e("DownloadUtil", e.getMessage(),e);
//		            }
//				}
//				length--;//实现递归
//				if(filePath.size()==2 || length==-1){
//					boo=false;//如果获取到了两个大于10K的日志，退出循环。
//				}
//			}
//		}
//		return filePath;
//    }
    //获取插件错误日志
    public static ArrayList<String> getLogList() {
        ArrayList<String> filePath = null;
        String logDir = getLogDir();
        try
        {
	        if (!TextUtils.isEmpty(logDir))
	        {
	            filePath = new ArrayList<String>();
	            File file = new File(logDir);
	            File[] files = file.listFiles();
	            // 当日志文件为两个或两个以上
	            if (null != files && files.length >= 1)
	            {
	            	
	            	for(int i=0;i<files.length;i++){
	            		if(files[i].getName().equals("icfcc_"+dateFormat.format(new Date())+".log")){
	                            String zipPath = files[i].getAbsolutePath()+".zip";
	                            ZipUtil.compress(files[i].getAbsolutePath(), zipPath);
	                            filePath.add(zipPath);
	                            break;
	            		}
	            	}
	            }
	        }
        }
        catch (IOException e)
        {
        	Logger.e("DownloadUtil", e.getMessage(),e);
        }
		return filePath;
    }
    
    //获取插件log目录
    public static String getLogDir() {
        String path = null;
        
    	File logfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IcfccLog");
        
        if(!logfile.exists()){
        	Logger.d("DownloadUtil","插件日志文件不存在");
        	return null;
        }
        path = logfile.getPath();
        return path;
    }
//    //获取applog目录
//	public static String getAppLogDir() {
//		String path = null;
//		File logDir = null;
//		if (AndroidVersionCheckUtils.hasFroyo()) {
//			logDir = MKExternalOverFroyoUtils.getDiskCacheDir(MKApplication.getApplication().getApplicationContext(), LOG_DIR);
//		} else {
//			logDir = MKExternalUnderFroyoUtils.getDiskCacheDir(MKApplication.getApplication().getApplicationContext(), LOG_DIR);
//		}
//		if (logDir != null) {
//			path = logDir.getPath();
//		}
//		return path;
//	}
    
	
//private ArrayList<String> getAppLog() {
//		// 得到所有错误日志
//		ArrayList<String> filePath = null;
//		// 获取插件日志
//		String logDir = LogUtils.getLogDir();
//		if (!TextUtils.isEmpty(logDir)) {
//			filePath = new ArrayList<String>();
//			File file = new File(logDir);
//			File[] files = file.listFiles();
//				
//			// 当日志文件为两个或两个以上
//			if (null != files && files.length >= 2) {
//				int length = files.length - 1;
//				if (files[length].getName().contains(".log")) {
//					filePath.add(files[length].getAbsolutePath());
//				}
//				if (files[length - 1].getName().contains(".log")) {
//
//					filePath.add(files[length - 1].getAbsolutePath());
//				}
//			}
//			// 当只有一个日志文件时
//			if (null != files && files.length == 1) {
//				int length = files.length - 1;
//				if (files[length].getName().contains(".log")) {
//					filePath.add(files[length].getAbsolutePath());
//					return filePath;
//				}
//			}
//		}
//		
//		return filePath;
//	}
}