package cn.z.ecash.commn;

public final class Util {
        private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
                        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        private Util() {
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
                        ret[x++] = HEX[0x0F & (v >> 4)];
                        ret[x++] = HEX[0x0F & v];
                }
                return new String(ret);
        }
        
        public static String intToHexString(int val) {
                final char[] ret = new char[4];

                int x = 0;
                final int v1 = (val & 0xFF00) >> 8;
                final int v2 = (val & 0x00FF);
                
                if (v1 != 0) {
                        ret[x++] = HEX[0x0F & (v1 >> 4)];
                        ret[x++] = HEX[0x0F & v1];
                }
                
                ret[x++] = HEX[0x0F & (v2 >> 4)];
                ret[x++] = HEX[0x0F & v2];

                return new String(ret,0,v1 != 0 ? 4 : 2);
        }

        public static String toHexStringR(byte[] d, int s, int n) {
                final char[] ret = new char[n * 2];

                int x = 0;
                for (int i = s + n - 1; i >= s; --i) {
                        final byte v = d[i];
                        ret[x++] = HEX[0x0F & (v >> 4)];
                        ret[x++] = HEX[0x0F & v];
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
}

