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
    	if(null == ba)
    		return null;
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
			Log.e("getNodes", "解析TLV出错", e);
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
	
	public static byte[] toBytes(int a) {
        return new byte[] { (byte) (0x000000ff & (a >>> 24)),
                        (byte) (0x000000ff & (a >>> 16)),
                        (byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
}

public static int toInt(byte[] b, int s, int n) {
        int ret = 0;

        final int e = s + n;
        for (int i = s; i < e; ++i) {
                ret <<= 8;
                ret |= b[i] & 0xFF;
        }
        return ret;
}

public static int toIntR(byte[] b, int s, int n) {
        int ret = 0;

        for (int i = s; (i >= 0 && n > 0); --i, --n) {
                ret <<= 8;
                ret |= b[i] & 0xFF;
        }
        return ret;
}

public static int toInt(byte... b) {
        int ret = 0;
        for (final byte a : b) {
                ret <<= 8;
                ret |= a & 0xFF;
        }
        return ret;
}

public static String toHexString(byte[] d, int s, int n) {
        final char[] ret = new char[n * 2];
        final int e = s + n;

        int x = 0;
        for (int i = s; i < e; ++i) {
                final byte v = d[i];
                ret[x++] = HEX_DIGITS[0x0F & (v >> 4)];
                ret[x++] = HEX_DIGITS[0x0F & v];
        }
        return new String(ret);
}

public static String intToHexString(int val) {
        final char[] ret = new char[4];

        int x = 0;
        final int v1 = (val & 0xFF00) >> 8;
        final int v2 = (val & 0x00FF);
        
        if (v1 != 0) {
                ret[x++] = HEX_DIGITS[0x0F & (v1 >> 4)];
                ret[x++] = HEX_DIGITS[0x0F & v1];
        }
        
        ret[x++] = HEX_DIGITS[0x0F & (v2 >> 4)];
        ret[x++] = HEX_DIGITS[0x0F & v2];

        return new String(ret,0,v1 != 0 ? 4 : 2);
}

public static String toHexStringR(byte[] d, int s, int n) {
        final char[] ret = new char[n * 2];

        int x = 0;
        for (int i = s + n - 1; i >= s; --i) {
                final byte v = d[i];
                ret[x++] = HEX_DIGITS[0x0F & (v >> 4)];
                ret[x++] = HEX_DIGITS[0x0F & v];
        }
        return new String(ret);
}

public static int parseInt(String txt, int radix, int def) {
        int ret;
        try {
                ret = Integer.valueOf(txt, radix);
        } catch (Exception e) {
                ret = def;
        }

        return ret;
}

public static String byteToHexString(byte[] b) {
 String hexString = "";
 for (int i = 0; i < b.length; i++) {
	 String hex = Integer.toHexString(b[i] & 0xFF);
   if (hex.length() == 1) {
     hex = '0' + hex;
   }
   hexString = hexString + hex.toUpperCase();
 }
 return hexString;
}

public static byte uniteBytes(byte src0, byte src1) {
  byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
    .byteValue();
  _b0 = (byte) (_b0 << 4);
  byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
    .byteValue();
  byte ret = (byte) (_b0 ^ _b1);
  return ret;
 }

 
 public static byte[] HexString2Bytes(String src) {
  byte[] ret = new byte[8];
  byte[] tmp = src.getBytes();
  for (int i = 0; i < 8; i++) {
   ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
  }
  return ret;
 }
 
 ///////
 /**
	 * fail result for find operation.
	 */
	public static final short TAG_NOT_FOUND = -1;
	
	public static int BCDtoInt(byte[] b, int s, int n) {
		int ret = 0;
		int tmp;

		final int e = s + n;
		for (int i = s; i < e; ++i) {
			tmp = (b[i] >> 4) & 0x0F;
			tmp = (tmp * 10) + (b[i] & 0x0F);
			ret = ret* 100 +  tmp;
		}
		return ret;
	}

	public static String toAmountString(float value) {
		return String.format("%.2f", value);
	}
	public static String toIntegerString(int value) {
		return String.format("%d", value);
	}
	/**
	 * 
	 * @param tag
	 * @param tlvList
	 * @param offset
	 * @param length
	 * @return the offset of the value in buffer,not offset of the tag.
	 */
	public static short findValueOffByTag(short tag, byte[] tlvList,
			short offset, short length) {
		short i = offset;
		length += offset;

		while (i < length) {
			// tag
			short tagTemp = (short) (tlvList[i] & 0x00FF);
			if ((short) (tagTemp & 0x001F) == 0x001F) {
				i++;
				tagTemp <<= 8;
				tagTemp |= (short) (tlvList[i] & 0x00FF);
			}
			i++;

			// length
			if (tlvList[i] == (byte) 0x81) {
				i++;
			}
			i++;

			// value
			if (tag == tagTemp) {
				return i;
			}

			i += (tlvList[(short) (i - 1)] & 0x00FF);
		}
		return TAG_NOT_FOUND;
	}
	
	public static short findandCopyValueByTag(short tag, byte[] tlvList,
			short offset, short length, byte[] destbuflv) {
		short toffset = 0;
		toffset = findValueOffByTag(tag, tlvList, offset, length);
		if (toffset == TAG_NOT_FOUND)
			return TAG_NOT_FOUND;
		else {
			destbuflv[0] = tlvList[(short) (toffset - 1)];
			System.arraycopy(tlvList, toffset, destbuflv, (short) 0x0001,
					(short) destbuflv[0]);
			return toffset;
		}
	}
	
	public static short GetTagInDOL(byte[] pDOL, short index) {
		short toffset = 1;
		short ttag = 0;
		short pDOLlength = (short) pDOL[0];

		if (index < 1)
			return (short) -1;
		while (index > 0) {
			if (toffset > pDOLlength)
				return (short) -1;
			ttag = (short) (pDOL[toffset++] & 0x00FF);
			if (((short) (ttag & 0x001F)) == ((short) 0x001F)) {
				ttag <<= 8;
				ttag |= (short) (pDOL[toffset++] & 0x00FF);
			}

			if (toffset > pDOLlength)
				return (short) -1;

			if ((short) pDOL[toffset] > (short) 0x0080) {
				toffset += (short) ((short) pDOL[toffset] - (short) 0x0080);
			} else {
				toffset += 1;
			}
			index--;
		}
		return ttag;
	}
	
	public static short arrayCopy(byte[] src, short srcOff, byte[] dest,short destOff, short length) {
		System.arraycopy(src, srcOff, dest, destOff, length);
		return (short) (destOff+length);
	}
	
	public static byte[] hexStringToByteArray(String data) {
		if (data == null || data.length() == 0 || (data.length() % 2) != 0) {
			return null;
		}
		int len = data.length() / 2;
		byte[] result = new byte[len];
		String tmp;
		for (int i = 0; i < len; i++) {
			tmp = data.substring(i * 2, (i + 1) * 2);
			try {
				result[i] = (byte) Integer.parseInt(tmp, 16);
			} catch (Exception e) {

				result[i] = 0x00;
			}
		}
		return result;
	}
	

}