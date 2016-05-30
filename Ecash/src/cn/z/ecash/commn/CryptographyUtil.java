package cn.z.ecash.commn;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 软件的方式 实现的密码学相关工具, 注意，使用此工具，必须将JDK jre\lib\security\下JAR local_policy.jar
 * US_export_policy.jar 包覆盖为安全Jar 包
 * 
 * @author tuchangdong 2013-5-15
 */
public class CryptographyUtil {
	public static final String ENCRYPTION_ALGO = "DESede";

	

	
	
	private static byte[] transferKeyFrom16To24(byte[] key){
		
		if(key.length != 16){
			return key;
		}
		
		byte[] tempKey = new byte[24];
		System.arraycopy(key, 0, tempKey, 0, key.length);
		System.arraycopy(key, 0, tempKey, key.length, 8);
		
		return tempKey;		
	}

	
	
	/**
	 * 转换密钥
	 * 
	 * @param key
	 *            二进制密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	private static Key toSingleDesKey(byte[] key) throws Exception {

		// 实例化DES密钥材料
		DESKeySpec dks = new DESKeySpec(key);

		// 实例化秘密密钥工厂
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("DES");

		// 生成秘密密钥
		SecretKey secretKey = keyFactory.generateSecret(dks);

		return secretKey;
	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptSingleDes(byte[] data, byte[] key) throws Exception {

		// 还原密钥
		Key k = toSingleDesKey(key);

		// 实例化
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);

		// 执行操作
		return cipher.doFinal(data);
	}
	
	/**
	 * 解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 *            
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static byte[] decryptSingleDesCBC(byte[] data, byte[] key,byte[] icv) throws Exception {

		// 还原密钥
		Key k = toSingleDesKey(key);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(icv);

		// 实例化
		Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

		// 初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k,ivParameterSpec);

		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptSingleDes(byte[] data, byte[] key) throws Exception {

		// 还原密钥
		Key k = toSingleDesKey(key);

		// 实例化
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);

		// 执行操作
		return cipher.doFinal(data);
	}
	
	/**
	 * 加密
	 * 
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 * @throws Exception
	 */
	public static byte[] encryptSingleDesCBC(byte[] data, byte[] key,byte[] icv) throws Exception {

		// 还原密钥
		Key k = toSingleDesKey(key);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(icv);

		// 实例化
		Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");

		// 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k,ivParameterSpec);

		// 执行操作
		return cipher.doFinal(data);
	}
	
	public static byte[] singleDesPlus3DES(byte[] des3key,byte[] data,byte[] icv) throws Exception{
		
		byte[] leftKey = new byte[8];
		byte[] rightKey = new byte[8];
		System.arraycopy(des3key, 0, leftKey, 0, 8);
		System.arraycopy(des3key, 8, rightKey, 0, 8);
		
		byte[] tmp = encryptSingleDesCBC(data,leftKey,icv);
		byte[] result = new byte[8];
		System.arraycopy(tmp, tmp.length-8, result, 0, 8);
		
		result = decryptSingleDes(result,rightKey);
		
		return encryptSingleDes(result,leftKey);
	}
	
	/**
	 * 根据公钥模值和公钥生成公钥对象
	 * @param n 公钥模值
	 * @param e 公钥
	 * @return
	 * @throws InvalidKeyException
	 */
	public static byte[] generateRSAPubKeyObbject(byte[] n,byte[] e) throws InvalidKeyException{
		
		BigInteger n_b = new BigInteger(n);
		BigInteger e_b = new BigInteger(e);
		
		RSAPublicKeySpec pubKey = new RSAPublicKeySpec(n_b,e_b);

		return ((Key) pubKey).getEncoded();
	}
	
	/**
	 * HmacMD5消息摘要
	 * 
	 * @param data
	 *            待做摘要处理的数据
	 * @param key
	 *            密钥
	 * @return byte[] 消息摘要 128位
	 * */
	public static byte[] encodeHmacMD5(byte[] data, byte[] key)
			throws Exception {
		// 还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, "HmacMD5");
		// 实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		// 初始化Mac
		mac.init(secretKey);
		// 执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA1消息摘要
	 * 
	 * @param data
	 *            待做摘要处理的数据
	 * @param key
	 *            密钥
	 * @return byte[] 消息摘要  160位
	 * */
	public static byte[] encodeHmacSHA(byte[] data, byte[] key)
			throws Exception {
		// 还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");
		// 实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		// 初始化Mac
		mac.init(secretKey);
		// 执行消息摘要处理
		return mac.doFinal(data);
	}
	
	/** 
     * 初始化HmacMD5的密钥 
     * @return byte[] 密钥 
     *  
     * */  
    public static byte[] initHmacMD5Key() throws Exception{  
        //初始化KeyGenerator  
        KeyGenerator keyGenerator=KeyGenerator.getInstance("HmacMD5");  
        //产生密钥  
        SecretKey secretKey=keyGenerator.generateKey();  
        //获取密钥  
        return secretKey.getEncoded();  
    } 
    
    
    /**
	 * 3des ECB 模式加密 NoPadding
	 * 
	 * @param des3key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] des3_ECB_encryption(byte[] des3key, byte[] data)
			throws Exception {
		
		des3key = transferKeyFrom16To24(des3key);
		
		DESedeKeySpec key = new DESedeKeySpec(des3key);
		SecretKeyFactory kf = SecretKeyFactory.getInstance(ENCRYPTION_ALGO);
		Key sk = kf.generateSecret(key);
		Cipher c = Cipher.getInstance(ENCRYPTION_ALGO + "/ECB/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, sk);
		return c.doFinal(data);
	}
}
