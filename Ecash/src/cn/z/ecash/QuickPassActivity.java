package cn.z.ecash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import cn.z.ecash.commn.ByteUtil;
import cn.z.ecash.commn.Logger;
import cn.z.ecash.commn.TLVEntity;
import cn.z.ecash.commn.Utils;
import cn.z.ecash.commn.Util;
import cn.z.ecash.nfc.CardManager;
import cn.z.ecash.nfc.PbocManager;
import cn.z.ecash.nfc.NfcUtil;
import android.R.layout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("NewApi")
public class QuickPassActivity extends Activity {
	private static final String LOGTAG = new String("QuickPass");
	private static final String TAG = new String("QuickPass");

	private static String CAPublickeyExp = new String("03");
	private static String CAPublickeyModuls = new String(
			"00EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5");

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;
	private PbocManager pbocmanager = null;

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	private String tag9F37value; // 终端不可预知数据
	private String RRAUTHDATA;

	// private String tag90value; //
	// private String tag9F32value; //
	// private String tag92value; //
	// private String tag8Fvalue; //
	// private String tag9F4Bvalue; //
	// private String tag9F5Dvalue; //
	// private String tag5F24value; //
	// private String tag5Avalue; //
	// private String tag9F07value; //
	// private String tag8Evalue; //
	// private String tag9F0Dvalue; //
	// private String tag9F0Evalue; //
	// private String tag9F0Fvalue; //
	// private String tag5F28value; //
	// private String tag9F46value; //
	// private String tag9F47value; //
	// private String tag9F48value; //
	// private String tag93value; //
	// private String tag5F25value; //
	// private String tag9F4Avalue; //
	// private String tag5F74value; //

	private Map<String, String> RRDataList = new HashMap();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(LOGTAG, "【onCreate】:start");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quick_pass);

		Button btnInputMoneyOK = (Button) findViewById(R.id.bt_input_purchasetmoney_ok);
		btnInputMoneyOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					eCashPurchase();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		onNewIntentCallByonCreate(getIntent());
		Log.i(LOGTAG, "【onCreate】:end");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardManager.FILTERS, CardManager.TECHLISTS);

	}

	/**
	 * 该方法调用时需要重新实例化 PbocManager 实例
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(LOGTAG, "【onNewIntent】:Clear PbocManager Instance");
		PbocManager.clearInstance();
		onNewIntentCallByonCreate(intent);
	}

	/**
	 * 该方法在onCreate方法中调用，PbocManag实例可以已经存在的。
	 * 
	 * @param intent
	 */
	protected void onNewIntentCallByonCreate(Intent intent) {
		pbocmanager = PbocManager.getInstance();
		if (null != pbocmanager) {
			Log.i(LOGTAG, "【onNewIntent】:PbocManager Instance != null");
			return;
		}
		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (p == null) {
			Log.i(LOGTAG, "【onNewIntent】:Parcelable == null");
			return;
		}
		Log.i(LOGTAG, "【onNewIntent】:Parcelable != null");
		PbocManager.clearInstance();
		pbocmanager = PbocManager.getInstance(p, res);
		Log.i(LOGTAG, "【onNewIntent】:PbocManager.getInstance" + pbocmanager);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * QuickPass 交易流程
	 * 
	 * @throws Exception
	 */
	private void eCashPurchase() throws Exception {
		processforquickpass();
		// showdebuginfo();
	}

	private int processforquickpass() throws Exception {
		int purchaseresult = -13;
		int toff = 0;

		pbocmanager = PbocManager.getInstance();
		if (pbocmanager == null) {
			return purchaseresult;
		}

		purchaseresult++;
		String fci = pbocmanager.SelectdefaultApplet();
		if (null == fci) {
			Log.i(LOGTAG, "【processforquickpass】:select err");
			return purchaseresult;
		}
		Log.i(LOGTAG, "【processforquickpass】:fci-->" + fci);

		byte[] fcibytes = Utils.bytesFromHexStringNoBlank(fci);
		int pdoloff = getPBOCPDOL(fcibytes);
		byte[] PDOLbytes = new byte[fcibytes[pdoloff - 1]];
		System.arraycopy(fcibytes, pdoloff, PDOLbytes, 0, PDOLbytes.length);
		Log.i(LOGTAG, "【processforquickpass】:PDOLoff-->" + pdoloff
				+ "\nPDOL-->" + NfcUtil.toHexString(PDOLbytes));

		List<String> PDOLlist = pasePdol(PDOLbytes);
		byte[] GPOcmd = getGPOData(PDOLlist);
		Log.i(LOGTAG,
				"【processforquickpass】:GPOcmd-->" + NfcUtil.toHexString(GPOcmd));

		String GPOres = sendGPOData(GPOcmd);
		String strAIP = "";
		String strAFL = "";
		if ("77".equals(GPOres.substring(0, 2))) {
			byte[] gporesbytes = Util.HexString2Bytes(GPOres);
			toff = 1;
			if ((byte) 0x81 == gporesbytes[toff++]) {
				toff++;
			}
			short aipoff = NfcUtil.findValueOffByTag((short) 0x82, gporesbytes,
					(short) toff, (short) gporesbytes[toff - 1]);
			if (aipoff < 0) {
				Log.i(LOGTAG, "【processforquickpass】:AIP not found");
				return purchaseresult;
			}
			strAIP = GPOres.substring(aipoff * 2, aipoff * 2 + 4);
			short afloff = NfcUtil.findValueOffByTag((short) 0x94, gporesbytes,
					(short) toff, (short) gporesbytes[toff - 1]);
			if (aipoff < 0) {
				Log.i(LOGTAG, "【processforquickpass】:AFL not found");
				return purchaseresult;
			}
			strAFL = GPOres.substring(afloff * 2, afloff * 2
					+ gporesbytes[afloff - 1] * 2);

		} else {
			strAIP = GPOres.substring(4, 8);
			strAFL = GPOres.substring(8);
		}
		Log.i(LOGTAG, "【processforquickpass】:GPOret-->" + GPOres + "AFL-->"
				+ strAFL);

		sendAndParseRR(strAFL);
		Log.i(LOGTAG, "【processforquickpass】:read record");
		RSAAuth(strAIP);
		String balance = pbocmanager.getBalance();
		showDialog("消费成功", "当前余额：" + balance + "元.");
		return purchaseresult;
	}

	private void RSAAuth(String pAIP) throws Exception {
		String IPKCertDec = RSADecrypt(CAPublickeyModuls, CAPublickeyExp,
				RRDataList.get(getResources().getString(R.string.tag90)));
		if (!IPKCertDec.startsWith("6A")) {
			Log.i(LOGTAG, "【RSAAuth】:发卡行公钥证书解密错误，不是以6A开始\n" + IPKCertDec);
			return;
		}
		if (!IPKCertDec.endsWith("BC")) {
			Log.i(LOGTAG, "【RSAAuth】:发卡行公钥证书解密错误，不是以BC结尾\n" + IPKCertDec);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:IPK公钥证书明文\n" + IPKCertDec);
		int certlen = IPKCertDec.length();
		String IPKHash_Card = IPKCertDec.substring((certlen - 42),
				(certlen - 2));
		String IPKModuls =IPKCertDec.substring(30, (certlen - 42))+ RRDataList.get(getResources().getString(R.string.tag92));
		String IPKExp = RRDataList.get(getResources().getString(R.string.tag9F32));
		String IPKHashData = IPKCertDec.substring(2, (certlen - 42))
				+ RRDataList.get(getResources().getString(R.string.tag92))
				+ IPKExp;
		Log.i(LOGTAG, "【RSAAuth】:IPK公钥证书Hash元数据\n" + IPKHashData);
		String tmpHash = getSha1(IPKHashData);
		if (!tmpHash.equals(IPKHash_Card)) {
			Log.i(LOGTAG, "【RSAAuth】:发卡行公钥证书Hash检验失败\n期望值:" + IPKHash_Card
					+ "\n实际值：" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:发卡行公钥证书Hash检验成功\n期望值:" + IPKHash_Card
				+ "\n实际值：" + tmpHash);
		
		String ICCPKCertDec = RSADecrypt(IPKModuls, IPKExp,
				RRDataList.get(getResources().getString(R.string.tag9F46)));
		if (!ICCPKCertDec.startsWith("6A")) {
			Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书解密错误，不是以6A开始\n" + IPKCertDec);
			return;
		}
		if (!ICCPKCertDec.endsWith("BC")) {
			Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书解密错误，不是以BC结尾\n" + IPKCertDec);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书明文\n" + ICCPKCertDec);
		certlen = ICCPKCertDec.length();
		String ICCPKHash_Card = ICCPKCertDec.substring((certlen - 42),
				(certlen - 2));
		String ICCPKModuls =ICCPKCertDec.substring(42, (certlen - 42))+ RRDataList.get(getResources().getString(R.string.tag9F48));
		String ICCPKExp = RRDataList.get(getResources().getString(R.string.tag9F47));
		
		String ICCPKHashData = ICCPKCertDec.substring(2, (certlen - 42))
				+ RRDataList.get(getResources().getString(R.string.tag9F48))
				+ ICCPKExp
				+ RRAUTHDATA
				+ pAIP;
		
		Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书Hash元数据\n" + ICCPKHashData);
		tmpHash = getSha1(ICCPKHashData);
		if (!tmpHash.equals(ICCPKHash_Card)) {
			Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书Hash检验失败\n期望值:" + ICCPKHash_Card
					+ "\n实际值：" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书Hash检验成功\n期望值:" + ICCPKHash_Card
				+ "\n实际值：" + tmpHash);
		
		String DDADataDec = RSADecrypt(ICCPKModuls, ICCPKExp,
				RRDataList.get(getResources().getString(R.string.tag9F4B)));
		if (!ICCPKCertDec.startsWith("6A")) {
			Log.i(LOGTAG, "【RSAAuth】:DDA签名解密错误，不是以6A开始\n" + DDADataDec);
			return;
		}
		if (!ICCPKCertDec.endsWith("BC")) {
			Log.i(LOGTAG, "【RSAAuth】:DDA签名解密错误，不是以BC结尾\n" + DDADataDec);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:DDA签名明文\n" + DDADataDec);
		certlen = DDADataDec.length();
		String DDAHash_Card = DDADataDec.substring((certlen - 42),
				(certlen - 2));
		
		String DDAHashData = DDADataDec.substring(2, (certlen - 42))
				+ tag9F37value;
		
		Log.i(LOGTAG, "【RSAAuth】:DDA签名Hash元数据\n" + DDAHashData);
		tmpHash = getSha1(DDAHashData);
		if (!tmpHash.equals(DDAHash_Card)) {
			Log.i(LOGTAG, "【RSAAuth】:DDA签名Hash检验失败\n期望值:" + DDAHash_Card
					+ "\n实际值：" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:DDA签名Hash检验成功\n期望值:" + DDAHash_Card
				+ "\n实际值：" + tmpHash);
	}

	/**
	 * 发送read record指令并解析响应
	 * 
	 * @throws Exception
	 */
	private void sendAndParseRR(String pAFL) throws Exception {
		Logger.v(TAG, "发送read record指令");
		DecimalFormat df;
		String tmpvalue;
		String tmptag;
		RRDataList.clear();
		RRAUTHDATA = "";
		try {
			for (int i = 0; i < pAFL.length() - 4;) {
				df = new DecimalFormat("00");
				String hexString = pAFL.substring(i, i + 8);
				byte[] AFLbytes = NfcUtil.hexStringToByteArray(hexString);
				int start = AFLbytes[1];
				int end = AFLbytes[2];
				int authdatarr = AFLbytes[3];
				AFLbytes[0] |= 0x04;
				String P1 = "";
				String P2 = NfcUtil.toHexString(AFLbytes).substring(0, 2);
				int SFI = (int) ((byte) 0x1F & (byte) (AFLbytes[0] >> 3));
				String res = "";
				for (int j = start; j <= end; j++) {
					P1 = df.format(j);
					StringBuffer sbrr = new StringBuffer();
					sbrr.append(getResources().getString(R.string.RRCLA));
					sbrr.append(getResources().getString(R.string.RRINS));
					sbrr.append(P1);
					sbrr.append(P2);
					sbrr.append(getResources().getString(R.string.RRLC));
					byte[] rrcom = Utils.bytesFromHexStringNoBlank(sbrr
							.toString());
					byte[] record = Utils.bytesFromHexStringNoBlank(pbocmanager
							.sendAPDU(rrcom));

					String str3 = Utils.toHexStringNoBlank(record);
					if(authdatarr > 0){
						if("81".equals(str3.substring(2,4))){
							RRAUTHDATA += str3.substring(6,(str3.length()-4));
						}else{
							RRAUTHDATA += str3.substring(4,(str3.length()-4));
						}
						authdatarr --;
					}
					if (record.length > 2) {// 成功响应
						List<TLVEntity> list = Utils.getNodes(str3);
						Logger.v(TAG, "读取应用数据 SFI = " + SFI + ",RecordNum = "
								+ P1);
						for (int k = 0; k < list.size(); k++) {
							tmptag = Util.intToHexString(list.get(k).getTag());
							tmpvalue = NfcUtil.toHexString(list.get(k)
									.getValue(), 0, list.get(k).length);
							RRDataList.put(tmptag, tmpvalue);
						}
					} else {
						Logger.v(TAG, "读应用数据 SFI = " + SFI + ",RecordNum = "
								+ P1 + "指令反馈失败，反馈结果为空");
						// continueFlag = false;
						// failReasion = "读取个人化数据失败。";
						break;
					}
				}
				i = i + 8;
			}
			Logger.i(TAG, "记录文件信息：\n" + RRDataList.toString());
		} catch (Exception e) {
			Logger.e(TAG, "发送read record指令并解析响应 异常", e);
			throw e;
		}
	}

	private int checkACType() {
		return 0;
	}

	/**
	 * 发送GPO指令，并解析响应
	 * 
	 * @param gpocom
	 *            GPO指令
	 * @throws Exception
	 */
	private String sendGPOData(byte[] gpocom) throws Exception {
		Logger.v(TAG, "发送GPO指令");
		try {
			byte[] returnMsg2 = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(gpocom));
			if (returnMsg2 == null)
				return null;
			String hexString = Utils.toHexStringNoBlank(returnMsg2);
			Logger.v(TAG, "GPO指令反馈内容：" + hexString);
			return hexString;
		} catch (Exception e) {
			Logger.e(TAG, "发送GPO指令异常", e);
			throw e;
		}
	}

	/**
	 * 根据PDOL数据组织GPO指令
	 * 
	 * @param list
	 *            PDOL数据
	 * @return byte[] GPO指令
	 */
	private byte[] getGPOData(List<String> list) {
		Logger.v(TAG, "根据PDOL数据包含内容组织GPO指令");

		StringBuffer gpodata = new StringBuffer();
		gpodata.append(getResources().getString(R.string.GPOCLA));
		gpodata.append(getResources().getString(R.string.GPOINS));
		gpodata.append(getResources().getString(R.string.GPOP1));
		gpodata.append(getResources().getString(R.string.GPOP2));
		String gpodatalen = "";
		String pdoldatastrlen = "";
		tag9F37value = "";

		if (list != null) {
			StringBuffer datastr = new StringBuffer();
			String pd = null;
			for (int i = 0; i < list.size(); i++) {
				pd = list.get(i);
				// 终端交易属性
				if (getResources().getString(R.string.tag9F66).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.qtag9F66value));
					continue;
				}
				// 电子现金终端支持指示器
				if (getResources().getString(R.string.tag9F7A).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag9F7Avalue));
					continue;
				}
				// 电子现金终端交易限额
				if (getResources().getString(R.string.tag9F7B).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag9F7Bvalue));
					continue;
				}
				// 授权金额
				if (getResources().getString(R.string.tag9F02).equals(pd)) {
					DecimalFormat df = new DecimalFormat("000000000000");
					EditText inamount = (EditText) findViewById(R.id.et_input_purchase_money);
					BigDecimal amount = new BigDecimal(inamount.getText()
							.toString());
					datastr.append(df.format(amount.multiply(new BigDecimal(
							"100"))));
					continue;
				}
				// 其它金额
				if (getResources().getString(R.string.tag9F03).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag9F03value));
					continue;
				}
				// 终端国家代码
				if (getResources().getString(R.string.tag9F1A).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag9F1Avalue));
					continue;
				}
				// 终端验证结果
				if (getResources().getString(R.string.tag95).equals(pd)) {
					datastr.append(getResources()
							.getString(R.string.tag95value));
					continue;
				}
				// 交易货币代码
				if (getResources().getString(R.string.tag5F2A).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag5F2Avalue));
					continue;
				}
				// 交易日期
				if (getResources().getString(R.string.tag9A).equals(pd)) {
					SimpleDateFormat dfdate = new SimpleDateFormat("yyMMdd");
					datastr.append(dfdate.format(new Date()));
					continue;
				}
				// 交易类型
				if (getResources().getString(R.string.tag9C).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.qtag9Cvalue));
					continue;
				}
				// 终端不可预知数据
				if (getResources().getString(R.string.tag9F37).equals(pd)) {
					tag9F37value = randomHexstr();
					datastr.append(tag9F37value);
					continue;
				}
				// CAPP 交易指示位，固定不支持
				if (getResources().getString(R.string.tagDF60).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tagDF60value));
					continue;
				}
			}

			StringBuffer sb = new StringBuffer();
			if (Integer.toHexString(datastr.toString().length() / 2).length() == 1) {
				sb.append("0");
				sb.append(Integer.toHexString(datastr.toString().length() / 2));
			} else {
				sb.append(Integer.toHexString(datastr.toString().length() / 2));
			}
			pdoldatastrlen = sb.toString();
			StringBuffer sb1 = new StringBuffer();
			if (Integer.toHexString(datastr.toString().length() / 2 + 2)
					.length() == 1) {
				sb1.append("0");
				sb1.append(Integer
						.toHexString(datastr.toString().length() / 2 + 2));
			} else {
				sb1.append(Integer
						.toHexString(datastr.toString().length() / 2 + 2));
			}
			gpodatalen = sb1.toString();

			// datastr.append("00");
			gpodata.append(gpodatalen);
			gpodata.append(getResources().getString(R.string.PDOLTag));
			gpodata.append(pdoldatastrlen);
			gpodata.append(datastr);
		} else {// 如果PDOL不存在，则命令数据域为“8300”
			gpodata.append("02");
			gpodata.append(getResources().getString(R.string.PDOLTag));
			gpodata.append("00");
		}
		gpodata.append("00");
		Logger.v(TAG, "GPO指令:" + gpodata.toString());
		return Utils.bytesFromHexStringNoBlank(gpodata.toString());
	}

	/**
	 * 随机生成一个十六进制字符串
	 * 
	 * @return 十六进制字符串
	 */
	private String randomHexstr() {
		StringBuffer hexstr = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			int j = (int) (Math.random() * 16);
			hexstr.append(HEX_DIGITS[j]);
		}
		return hexstr.toString();
	}

	/**
	 * 解析select响应，获得PDOL数据
	 * 
	 * @return PDOL数据中标签集合
	 */
	private List<String> pasePdol(byte[] pdolmsg) {
		Log.i(LOGTAG, "解析PDOL数据");
		List<String> pdolcontext = null;
		if (pdolmsg != null) {
			pdolcontext = new ArrayList<String>();
			String pdoltemp = Utils.toHexStringNoBlank(pdolmsg);
			if (pdoltemp.contains(getResources().getString(R.string.tag9F66))) {
				Log.i(LOGTAG, "PDOL数据包含9F66：终端交易属性");
				pdolcontext.add(getResources().getString(R.string.tag9F66));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7A))) {
				Log.i(LOGTAG, "PDOL数据包含9F7A：电子现金终端支持指示器");
				pdolcontext.add(getResources().getString(R.string.tag9F7A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7B))) {
				Log.i(LOGTAG, "PDOL数据包含9F7B：电子现金终端交易限额");
				pdolcontext.add(getResources().getString(R.string.tag9F7B));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F02))) {
				Log.i(LOGTAG, "PDOL数据包含9F02：授权金额");
				pdolcontext.add(getResources().getString(R.string.tag9F02));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F03))) {
				Log.i(LOGTAG, "PDOL数据包含9F03：其它金额");
				pdolcontext.add(getResources().getString(R.string.tag9F03));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F1A))) {
				Log.i(LOGTAG, "PDOL数据包含9F1A：终端国家代码");
				pdolcontext.add(getResources().getString(R.string.tag9F1A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag95))) {
				Log.i(LOGTAG, "PDOL数据包含95：终端验证结果");
				pdolcontext.add(getResources().getString(R.string.tag95));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag5F2A))) {
				Log.i(LOGTAG, "PDOL数据包含5F2A：交易货币代码");
				pdolcontext.add(getResources().getString(R.string.tag5F2A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9A))) {
				Log.i(LOGTAG, "PDOL数据包含9A：交易日期");
				pdolcontext.add(getResources().getString(R.string.tag9A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9C))) {
				Log.i(LOGTAG, "PDOL数据包含9C：交易类型");
				pdolcontext.add(getResources().getString(R.string.tag9C));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F37))) {
				Log.i(LOGTAG, "PDOL数据包含9F37：终端不可预知数据");
				pdolcontext.add(getResources().getString(R.string.tag9F37));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tagDF60))) {
				Log.i(LOGTAG, "PDOL数据包含DF60：CAPP 交易指示位");
				pdolcontext.add(getResources().getString(R.string.tagDF60));
			}
		}
		return pdolcontext;
	}

	private int getPBOCPDOL(byte[] tbyte) {
		int toff = 0;
		int resoff = -1;
		if ((byte) 0x6F == tbyte[toff++]) {
			if ((byte) 0x81 == tbyte[toff++]) {
				toff++;
			}
			toff = NfcUtil.findValueOffByTag((short) 0xA5, tbyte, (short) toff,
					(short) tbyte[toff - 1]);
			if (toff < 0)
				return -1;
			int toffA5 = toff;

			// PDOL
			toff = NfcUtil.findValueOffByTag((short) 0x9F38, tbyte,
					(short) toff, (short) tbyte[toffA5 - 1]);
			if (toff < 0)
				return -1;
			resoff = toff;

			toff = NfcUtil.findValueOffByTag((short) 0xBF0C, tbyte,
					(short) toffA5, (short) tbyte[toffA5 - 1]);
			if (toff < 0)
				return -1;
			int BF0Coff = toff;

			toff = NfcUtil.findValueOffByTag((short) 0x9F4D, tbyte,
					(short) toff, (short) tbyte[BF0Coff - 1]);
			if (toff < 0)
				return -1;
		}
		return resoff;
	}

	/**
	 * 
	 * 方法描述：
	 */
	private void showDialog(String title, String context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				QuickPassActivity.this).setTitle(title).setMessage(context);
		AlertDialog dialog = builder.show();
	}

	private void showdebuginfo() {
		pbocmanager = PbocManager.getInstance();
		if (null == pbocmanager) {
			Log.i(LOGTAG, "【DEBUG】:no card");
			return;
		}
		if (null == pbocmanager.SelectdefaultApplet()) {
			Log.i(LOGTAG, "【DEBUG】:select card err");
			return;
		}

		showDialog(new String("QuickPass"),
				new String("ATC=" + pbocmanager.getTag("9F36")));

	}

	public static String RSADecrypt(String pkeymoduls, String pkeyexp,
			String encdata) throws Exception {
		Log.i(LOGTAG, "【RSADecrypt】\nmoduls:"+pkeymoduls
				+"\nexp:"+pkeyexp
				+"\ndata:"+encdata);

		byte[] pubkeymoduls = ByteUtil.hexStringToByteArray("00"+pkeymoduls);
		byte[] pubkeyexp = ByteUtil.hexStringToByteArray(pkeyexp);

		BigInteger b1 = new BigInteger(pubkeymoduls);
		BigInteger b2 = new BigInteger(pubkeyexp);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec keyspec = new RSAPublicKeySpec(b1, b2);
		PublicKey pubkey = keyFactory.generatePublic(keyspec);

		// 对数据解密密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm()
				+ "/None/NoPadding", getProvierName());

		cipher.init(Cipher.DECRYPT_MODE, pubkey);

		byte[] decodedata = cipher.doFinal(ByteUtil
				.hexStringToByteArray(encdata));
		return ByteUtil.byteArrayToHexString(decodedata);
	}

	private static String PROVIDER_NAME = "BC";
	private static boolean ISDECIDE = false;

	public static String getProvierName() {
		if (ISDECIDE && Security.getProvider(PROVIDER_NAME) != null) {
			return PROVIDER_NAME;
		} else {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			ISDECIDE = true;
			return PROVIDER_NAME;
		}

	}

	public static String getSha1(String data) throws Exception {
		MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
		digest.update(ByteUtil.hexStringToByteArray(data));
		byte[] ipkmd = digest.digest();
		String localIPKHash = ByteUtil.byteArrayToHexString(ipkmd);
		return localIPKHash;
	}
}
