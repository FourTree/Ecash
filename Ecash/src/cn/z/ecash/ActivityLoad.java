package cn.z.ecash;

import cn.z.ecash.commn.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.z.ecash.nfc.CardManager;
import cn.z.ecash.nfc.PbocManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ActivityLoad extends Activity {
	private static final String LOGTAG = new String("LOAD");

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;

	public final static int RESULT_CODE = 1;
	TextView tvshowparm;
	EditText etInput;
	private PbocManager pbocmanager = null;
	private String balance;

	private static final String TAG = "ActivityLoad";

	private String pattern = "000000000000";
	private DecimalFormat df;
	private DateFormat dfdate;
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private Map<String, String> RRDataList = new HashMap();
	private String RRAUTHDATA;//静态认证数据
	private String AIP; // 应用交互特征
	private String AFL; // 应用文件定位器
	private byte[] FCIMsg = null;
	private byte[] PDOLMsg = null;

	private String TVR; // 终端验证结果
	private String byte1; // TVR字节1
	private String byte2; // TVR字节2
	private String byte3; // TVR字节3
	private String byte4; // TVR字节4
	private String byte5; // TVR字节5

	private String tag9F27value; // 密文信息数据
	private String tag9F36value; // 应用交易计数器（ATC）
	private String tag9F26value; // 应用密文（AC）
	private String tag9F10value; // 发卡行应用数据

	private String tag9F37value; // 终端不可预知数据

	private String authData; // 认证数据
	private String issuerScript; // 发卡行脚本
	
	private boolean continueFlag = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);
		Bundle tbdl = this.getIntent().getExtras();
		String param = tbdl.getString("showstring");

		tvshowparm = (TextView) findViewById(R.id.tv_showdebuginfo);
		tvshowparm.setText(param);

		etInput = (EditText) findViewById(R.id.et_credit_for_load_inputmoney);

		Button btnCreditForLoadOK = (Button) findViewById(R.id.btn_credit_for_load_ok);
		btnCreditForLoadOK.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// retMainActivity();
				creditforload();
			}
		});
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		onNewIntentCallByonCreate(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(LOGTAG, "【onResume】:start");

		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardManager.FILTERS, CardManager.TECHLISTS);
		Log.i(LOGTAG, "【onResume】:end");
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

	void retMainActivity() {
		String strinput = etInput.getText().toString();
		Intent intent = new Intent();
		intent.putExtra("inputloamoney", strinput);//
		setResult(RESULT_CODE, intent);//
		finish();

	}

	void creditforload() {
		int inputlen = etInput.getText().length();
		if ((0 == inputlen) || (6 < inputlen)) {
			tvshowparm
					.setText("input wrong!!! please input the right nunber...");
			return;
		}
		String strinputmoney = etInput.getText().toString();
		int result = processcreditforload(getIntent());
		if (result < 0) {
			// 充值异常
			showDialog("充值失败", "未知错误");
		} else {
			// 执行发卡行脚本成功
			String afterbalance = pbocmanager.getBalance();
			showDialog("充值成功", "当前余额:" + afterbalance + "元");
		}
	}

	protected int processcreditforload(Intent intent) {
		int loadresult = -13;
		boolean openFlag = false;
		pbocmanager = PbocManager.getInstance();
		if (pbocmanager == null)
			return loadresult;
		String fci = pbocmanager.SelectdefaultApplet();
		if (null == fci) {
			Log.i(LOGTAG, "【processcreditforload】:select err");
			return loadresult;
		}
		FCIMsg = Utils.bytesFromHexStringNoBlank(fci);

		openFlag = true;
		Log.i(LOGTAG, "【start】" + fci);

		balance = pbocmanager.getBalance();
		Log.i(LOGTAG, "【balacne】" + balance);

		try {
			// 判断PDOL
			boolean pdolFlag = false;
			if (openFlag && continueFlag) {
				pdolFlag = hasPDOL();
				Log.i(LOGTAG, "【hasPDOL】" + pdolFlag);

			} else {
				continueFlag = false;
			}
			// 步骤1--PDOL
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 重发select指令，获取pdol
			if (!pdolFlag && continueFlag) {
				reSendSelect();
				Log.i(LOGTAG, "【reSelect】:done");
			}
			// 步骤2--reselect
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 解析select指令响应，获得PDOL数据
			List<String> pdoldata = null;
			if (continueFlag) {
				pdoldata = pasePdol();
			}

			// 步骤3--get PDOL
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 根据PDOL组织GPO指令
			byte[] gpodata = null;
			if (continueFlag && pdoldata != null) {
				gpodata = getGPOData(pdoldata);
				Log.i(LOGTAG, "【GPO】:data-->" + Utils.toHexString(gpodata));

			} else {
				continueFlag = false;
			}
			// 步骤4--get get GPO CMD
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 发送GPO指令并解析指令响应
			if (continueFlag && gpodata != null) {
				sendGPOData(gpodata);
				Log.i(LOGTAG, "【GPO】:done");
			} else {
				continueFlag = false;
			}
			// 步骤5--send GPO CMD
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 发送Read Record指令并解析指令响应
			if (continueFlag) {
				sendAndParseRR(AFL);
				Log.i(LOGTAG, "【readRecode】:done");
			}
			// 步骤6--read record
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}
			// 得到标签95的值
			if (continueFlag) {
				TVR = parseTag95();
			}
			// 步骤7--get TVR
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 终端行为分析（充值为联机交易不做终端行为分析）
			// 步骤8--
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 获取GENERATE AC 指令 做卡片行为分析
			String acCommond = null;
			if (continueFlag) {
				acCommond = getGACCommond();
				Log.i(LOGTAG, "【GAC】:data-->" + acCommond);

			}
			// 步骤9--get GAC
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 发送GENERATE AC 指令并解析响应
			if (continueFlag && acCommond != null) {
				sendAndParseAC(acCommond);
				Log.i(LOGTAG, "【GAC】:done");
			} else {
				continueFlag = false;
			}
			// 步骤10--send GAC CMD
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			df = new DecimalFormat(pattern);
			BigDecimal balancestr = new BigDecimal(balance);

			EditText inamount = (EditText) findViewById(R.id.et_credit_for_load_inputmoney);
			BigDecimal amount = new BigDecimal(inamount.getText().toString());

			String strBalance = df.format(balancestr.multiply(new BigDecimal(
					"100")));
			String strAAM = df.format(amount.multiply(new BigDecimal("100")));
			Log.i(LOGTAG, "【ATC】:" + tag9F36value + "\n【ECBalance】:"
					+ strBalance + "\n【AAM】:" + strAAM + "\n【AC】:"
					+ tag9F26value);

			/**
			 * 计算发卡行脚本
			 */
			Map<String, String> res = getIssuerData(tag9F36value, strBalance,
					strAAM, tag9F26value);
			authData = res.get("IssAuthData");
			issuerScript = res.get("IssuerScript");

			// 外部认证
			if (continueFlag) {
				externalAuth();
			}
			// 步骤11--external auth
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 交易结束处理
			if (continueFlag) {
				doLoadEnd();
			}
			// 步骤12--GAC
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}

			// 发卡行脚本执行
			if (continueFlag) {
				exeCardBankScript();
			}
			// 步骤13--issuer script
			if (continueFlag) {
				loadresult++;
			} else {
				return loadresult;
			}
		} catch (Exception e) {
			Log.e(TAG, "充值时异常", e);
		}
		return loadresult;
	}

	/**
	 * 执行发卡行脚本
	 * 
	 * @throws Exception
	 */
	private void exeCardBankScript() throws Exception {
		Log.v(TAG, "执行发卡行脚本");
		List<String> scriptStr = new ArrayList<String>();

		int scriptlen = 0;
		String script = null;
		for (int i = issuerScript.indexOf("86"); i < issuerScript.length();) {
			scriptlen = Integer.parseInt(issuerScript.substring(i + 2, i + 4),
					16);
			script = issuerScript.substring(i + 4, i + 4 + scriptlen * 2);
			scriptStr.add(script);
			i = i + 4 + scriptlen * 2;
		}

		try {
			for (int i = 0; i < scriptStr.size(); i++) { // 循环执行所有脚本
				byte[] result = null;
				result = Utils.bytesFromHexStringNoBlank(pbocmanager
						.sendAPDU(Utils.bytesFromHexStringNoBlank(scriptStr
								.get(i))));

				String resultStr = Utils.toHexStringNoBlank(result);
				Log.v(TAG, "执行发卡行脚本命令" + scriptStr.get(i) + "反馈内容:"
						+ resultStr);
				if (result.length == 0 || result == null) {
					Log.v(TAG, "执行发卡行脚本命令" + scriptStr.get(i) + "反馈为空");
					continueFlag = false;
					break;
				}
				if ("9000".equals(resultStr)) {
					Log.v(TAG, "执行发卡行脚本命令" + scriptStr.get(i) + "反馈成功");
					continue;
				} else { // 一个脚本执行失败即停止循环，但交易继续
					Log.v(TAG, "执行脚本命令反馈失败");
					continueFlag = false;
					break;
				}
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "执行脚本命令失败", e);
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "执行脚本命令失败", e);
			throw e;
		}
	}

	/**
	 * 交易结束处理
	 * 
	 * @throws Exception
	 */
	private void doLoadEnd() throws Exception {
		Log.v(TAG, "交易结束处理");
		StringBuffer sbEndACComd = new StringBuffer();
		sbEndACComd.append(getResources().getString(R.string.ENDACCLA));
		sbEndACComd.append(getResources().getString(R.string.ENDACINS));
		sbEndACComd.append(getResources().getString(R.string.ENDACP1));
		sbEndACComd.append(getResources().getString(R.string.ENDACP2));

		StringBuffer sbEndACData = new StringBuffer();
		String tag8Dvalue = RRDataList.get(getResources().getString(R.string.tag8D));
		// 授权响应码（认证数据后两个字节）
		if (tag8Dvalue.contains(getResources().getString(R.string.tag8A))) {
			sbEndACData.append(authData.substring(authData.length() - 4));
		}
		// 授权金额9F02
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F02))) {
			df = new DecimalFormat(pattern);
			EditText inamount = (EditText) findViewById(R.id.et_credit_for_load_inputmoney);
			BigDecimal amount = new BigDecimal(inamount.getText().toString());
			sbEndACData
					.append(df.format(amount.multiply(new BigDecimal("100"))));
		}
		// 其它金额9F03
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F03))) {
			sbEndACData.append(getResources().getString(R.string.tag9F03value));
		}
		// 终端国家代码9F1A
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F1A))) {
			sbEndACData.append(getResources().getString(R.string.tag9F1Avalue));
		}
		// 终端验证结果95
		if (tag8Dvalue.contains(getResources().getString(R.string.tag95))) {
			sbEndACData.append(byte1 + byte2 + byte3 + byte4 + byte5);
		}
		// 交易货币代码5F2A
		if (tag8Dvalue.contains(getResources().getString(R.string.tag5F2A))) {
			sbEndACData.append(getResources().getString(R.string.tag5F2Avalue));
		}
		dfdate = new SimpleDateFormat("yyMMddHHmmss");
		// 交易日期9A
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9A))) {
			sbEndACData.append(dfdate.format(new Date()).substring(0, 6));
		}
		// 交易类型9C
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9C))) {
			sbEndACData.append(getResources().getString(R.string.tag9Cvalue));
		}
		// 终端不可预知数据9F37
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F37))) {
			if (tag9F37value == null) {
				tag9F37value = randomHexstr();
			}
			sbEndACData.append(tag9F37value);
		}
		// 交易时间9F21
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F21))) {
			sbEndACData.append(dfdate.format(new Date()).substring(6));
		}
		// 商户名称9F4E
		if (tag8Dvalue.contains(getResources().getString(R.string.tag9F4E))) {
			sbEndACData.append(getResources().getString(R.string.tag9F4Evalue));
		}
		// lc
		StringBuffer sb = new StringBuffer();
		if (Integer.toHexString(sbEndACData.length() / 2).length() == 1) {
			sb.append("0");
			sb.append(Integer.toHexString(sbEndACData.length() / 2));
		} else {
			sb.append(Integer.toHexString(sbEndACData.length() / 2));
		}
		String dataLen = sb.toString();

		sbEndACComd.append(dataLen);
		sbEndACComd.append(sbEndACData);
		sbEndACComd.append("00");
		Log.v(TAG, "交易结束处理指令:" + sbEndACComd.toString());
		try {
			byte[] gacResponseBytes = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(Utils.bytesFromHexStringNoBlank(sbEndACComd
							.toString())));

			Log.v(TAG, "交易结束处理指令反馈内容:" + Utils.toHexString(gacResponseBytes));
			if (gacResponseBytes.length <= 2) {
				Log.v(TAG, "交易结束处理指令反馈失败,指令反馈为空");
				continueFlag = false;
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "发送交易结束指令异常", e);
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "发送交易结束指令异常", e);
			throw e;
		}
	}

	/**
	 * 外部认证
	 * 
	 * @throws Exception
	 */
	private void externalAuth() throws Exception {
		boolean result = true;
		Log.v(TAG, "外部认证");
		StringBuffer sbAuthCommond = new StringBuffer();
		sbAuthCommond.append(getResources().getString(R.string.externalAuth));

		StringBuffer sb = new StringBuffer();
		if (Integer.toHexString(authData.length() / 2).length() == 1) {
			sb.append("0");
			sb.append(Integer.toHexString(authData.length() / 2));
		} else {
			sb.append(Integer.toHexString(authData.length() / 2));
		}
		String authDataLen = sb.toString();
		sbAuthCommond.append(authDataLen);
		sbAuthCommond.append(authData);
		Log.v(TAG, "外部认证指令:" + sbAuthCommond.toString());

		try {
			byte[] authResult = null;
			authResult = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(Utils.bytesFromHexStringNoBlank(sbAuthCommond
							.toString())));
			if (authResult.length == 0 || authResult == null) {
				Log.v(TAG, "外部认证指令反馈为空，外部认证失败");
				byte5 = "C0";// 发卡行认证失败，重置TVR btye5
				TVR = TVR.substring(TVR.length() - 2);
				TVR = TVR + byte5;
				continueFlag = false;
			} else {
				String resultStr = Utils.toHexStringNoBlank(authResult);
				if (!"9000".equals(resultStr)) {
					byte5 = "C0";// 发卡行认证失败，重置TVR btye5
					TVR = TVR.substring(TVR.length() - 2);
					TVR = TVR + byte5;
					Log.v(TAG, "外部认证指令反馈失败，反馈内容:" + resultStr);
					continueFlag = false;
				} else {
					Log.v(TAG, "外部认证成功");
				}
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "外部认证异常", e);
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "外部认证异常", e);
			throw e;
		}
	}

	/**
	 * 发送GENERATE AC指令并解析响应
	 * 
	 * @param commond
	 *            AC指令
	 * @throws Exception
	 */
	private void sendAndParseAC(String commond) throws Exception {
		Log.v(TAG, "发送GENERATE AC指令");
		try {
			byte[] gacResponseBytes = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(Utils.bytesFromHexStringNoBlank(commond)));

			Log.d(
					TAG,
					"GENERATE AC指令返回值="
							+ Utils.toHexStringNoBlank(gacResponseBytes));
			if (gacResponseBytes.length > 2) {
				String hexStrGAC = Utils.toHexStringNoBlank(gacResponseBytes);
				hexStrGAC = hexStrGAC.substring(0, hexStrGAC.length() - 4);
				Log.v(TAG, "GENERATE AC指令反馈成功，反馈内容:" + hexStrGAC);
				// 密文信息数据
				tag9F27value = hexStrGAC.substring(4, 6);
				Log.v(TAG, "密文信息数据:" + tag9F27value);
				// 应用交易计数器（ATC）
				tag9F36value = hexStrGAC.substring(6, 10);
				Log.v(TAG, "应用交易计数器（ATC）:" + tag9F36value);
				// 应用密文（AC）
				tag9F26value = hexStrGAC.substring(10, 26);
				Log.v(TAG, "应用密文（AC）:" + tag9F26value);
				// 发卡行应用数据
				tag9F10value = hexStrGAC.substring(26);
				Log.v(TAG, "发卡行应用数据:" + tag9F10value);
			} else {
				Log.v(TAG, "GENERATE AC指令反馈失败，指令返回为空");
				continueFlag = false;
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "发送GENERATE AC指令异常", e);
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "发送GENERATE AC指令异常", e);
			throw e;
		}
	}

	/**
	 * 获取GENERATE AC 指令
	 * 
	 * @return GENERATE AC 指令
	 */
	private String getGACCommond() {
		Log.v(TAG, "根据卡片风险管理数据对象列表1 CDOL1 [8C]组织GENERATE AC 指令");
		StringBuffer sbAC = new StringBuffer();
		// cla
		sbAC.append(getResources().getString(R.string.ACCLA));
		// ins
		sbAC.append(getResources().getString(R.string.ACINS));
		// p1
		sbAC.append(getResources().getString(R.string.ACP1));
		// p2
		sbAC.append(getResources().getString(R.string.ACP2));

		StringBuffer sbACData = new StringBuffer();
		String tag8Cvalue = RRDataList.get(getResources().getString(R.string.tag8C));
		Log.v(TAG, "卡片风险管理数据对象列表1 CDOL1 [8C]的值:" + tag8Cvalue);
		if (tag8Cvalue != null) {
			// 授权金额9F02
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F02))) {
				df = new DecimalFormat(pattern);
				EditText inamount = (EditText) findViewById(R.id.et_credit_for_load_inputmoney);
				BigDecimal amount = new BigDecimal(inamount.getText()
						.toString());
				sbACData.append(df.format(amount
						.multiply(new BigDecimal("100"))));
			}

			// 其它金额9F03
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F03))) {
				sbACData.append(getResources().getString(R.string.tag9F03value));
			}
			// 终端国家代码9F1A
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F1A))) {
				sbACData.append(getResources().getString(R.string.tag9F1Avalue));
			}
			// 终端验证结果95
			if (tag8Cvalue.contains(getResources().getString(R.string.tag95))) {
				sbACData.append(TVR);
			}
			// 交易货币代码5F2A
			if (tag8Cvalue.contains(getResources().getString(R.string.tag5F2A))) {
				sbACData.append(getResources().getString(R.string.tag5F2Avalue));
			}
			// 交易日期9A
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9A))) {
				dfdate = new SimpleDateFormat("yyMMddHHmmss");
				String date = dfdate.format(new Date());
				sbACData.append(date.substring(0, 6));
			}
			// 交易类型9C
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9C))) {
				sbACData.append(getResources().getString(R.string.tag9Cvalue));
			}
			// 终端不可预知数据9F37
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F37))) {
				if (tag9F37value == null) {
					tag9F37value = randomHexstr();
				}
				sbACData.append(tag9F37value);
			}
			// 交易时间9F21
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F21))) {
				String date = dfdate.format(new Date());
				sbACData.append(date.substring(6));
			}
			// 交易时间9F4E
			if (tag8Cvalue.contains(getResources().getString(R.string.tag9F4E))) {
				sbACData.append(getResources().getString(R.string.tag9F4Evalue));
			}
		}

		// lc
		StringBuffer sb = new StringBuffer();
		if (Integer.toHexString(sbACData.toString().length() / 2).length() == 1) {
			sb.append("0");
			sb.append(Integer.toHexString(sbACData.toString().length() / 2));
		} else {
			sb.append(Integer.toHexString(sbACData.toString().length() / 2));
		}
		String datalen = sb.toString();
		sbAC.append(datalen);

		// data
		sbAC.append(sbACData);
		sbAC.append("00");
		Log.v(TAG, "GENERATE AC 指令:" + sbAC.toString());
		return sbAC.toString();
	}

	/**
	 * 得到终端验证结果即95标签的值
	 * 
	 * @return 终端验证结果
	 * @throws Exception
	 */
	private String parseTag95() throws Exception {
		Log.v(TAG, "获得终端验证结果");
		StringBuffer sbTVR = new StringBuffer();
		// 字节1-------start
		byte1 = "80";
		Log.v(TAG, "终端验证结果字节1:" + byte1);
		sbTVR.append(byte1); // 充值不做DDA认证，TVR字节1的b8位为1，其他位为0（其中b5,即卡片出现在终端异常文件中因为没有终端异常文件接口所以置0）
		// 字节1-------end

		// 字节2-------start
		int TVRByte2 = 0; // 十进制整数最后转成十六进制数
		if (!getResources().getString(R.string.appversion).equals(RRDataList.get(getResources().getString(R.string.tag9F08)))) {
			TVRByte2 = TVRByte2 + 128; // IC 卡和终端应用版本不一致，TVR字节2的b8位为1
		}

		// 日期的判断方法不确定，TVR字节2的b7和b6位暂为0
		String tag9F07value = "FFC0";// ////////测试假数据
		char c = tag9F07value.charAt(2);
		if (c > '8') {
			TVRByte2 = TVRByte2 + 16; // 交易中有返现金额,AUC中字节2中b8位为1，即允许国内返现
		}
		byte[] atcbytes = null;
		try {
			// 终端发送取数据（GET DATA）命令读取卡片中的上次联机ATC寄存器[9F13]值
			Log.v(TAG, "终端发送取数据（GET DATA）命令80CA9F1300读取卡片中的上次联机ATC寄存器值");
			atcbytes = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(Utils.bytesFromHexStringNoBlank("80CA9F1300")));

		} catch (NumberFormatException e) {
			Log.e(TAG, "终端发送取数据（GET DATA）命令读取卡片中的上次联机ATC寄存器值", e);
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "终端发送取数据（GET DATA）命令读取卡片中的上次联机ATC寄存器值", e);
			throw e;
		}

		if (atcbytes.length > 2) {
			String hexstr = Utils.toHexString(atcbytes);
			Log.v(TAG, "卡片中的上次联机ATC寄存器值" + hexstr);
			if ("0000".equals(hexstr.substring(6))) {
				TVRByte2 = TVRByte2 + 8;
			}
		} else {
			Log.v(TAG, "终端发送取数据（GET DATA）命令读取卡片中的上次联机ATC寄存器值失败，返回值为空");
			continueFlag = false;
		}
		StringBuffer sb = new StringBuffer();
		if (Integer.toHexString(TVRByte2).length() == 1) {
			sb.append("0");
			sb.append(Integer.toHexString(TVRByte2));
		} else {
			sb.append(Integer.toHexString(TVRByte2));
		}
		byte2 = sb.toString();
		Log.v(TAG, "终端验证结果字节2:" + byte2);
		sbTVR.append(byte2);
		// 字节2-------end

		// 字节3-------start
		byte3 = "04";
		Log.v(TAG, "终端验证结果字节3:" + byte3);
		sbTVR.append(byte3);// 字节3中b3位为1，即输入联机pin，其他位为0
		// 字节3-------end

		// 字节4-------start
		byte4 = "08";
		Log.v(TAG, "终端验证结果字节4:" + byte4);
		sbTVR.append(byte4);// 字节4中b4位为1，即商户要求联机交易，其他位为0
		// 字节4-------end

		// 字节5-------start
		byte5 = "80";
		Log.v(TAG, "终端验证结果字节5:" + byte5);
		sbTVR.append(byte5);// 字节5中b8位为1，即使用缺省TDOL（其中b7位，即发卡行认证失败位先置0，之后会重置）
		// 字节5-------end
		Log.v(TAG, "终端验证结果:" + sbTVR.toString());
		return sbTVR.toString();
	}

	/**
	 * 发送read record指令并解析响应
	 * 
	 * @throws Exception
	 */
	/**
	 * 发送read record指令并解析响应
	 * 
	 * @throws Exception
	 */
	private void sendAndParseRR(String pAFL) throws Exception {
		Log.v(TAG, "发送read record指令");
		DecimalFormat df;
		String tmpvalue;
		String tmptag;
		RRDataList.clear();
		RRAUTHDATA = "";
		try {
			for (int i = 0; i < pAFL.length() - 4;) {
				df = new DecimalFormat("00");
				String hexString = pAFL.substring(i, i + 8);
				byte[] AFLbytes = Utils.hexStringToByteArray(hexString);
				int start = AFLbytes[1];
				int end = AFLbytes[2];
				int authdatarr = AFLbytes[3];
				AFLbytes[0] |= 0x04;
				String P1 = "";
				String P2 = Utils.toHexString(AFLbytes).substring(0, 2);
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
						Log.v(TAG, "读取应用数据 SFI = " + SFI + ",RecordNum = "
								+ P1);
						for (int k = 0; k < list.size(); k++) {
							tmptag = Utils.intToHexString(list.get(k).getTag());
							tmpvalue = Utils.toHexString(list.get(k)
									.getValue(), 0, list.get(k).length);
							RRDataList.put(tmptag, tmpvalue);
						}
					} else {
						Log.v(TAG, "读应用数据 SFI = " + SFI + ",RecordNum = "
								+ P1 + "指令反馈失败，反馈结果为空");
						// continueFlag = false;
						// failReasion = "读取个人化数据失败。";
						break;
					}
				}
				i = i + 8;
			}
			Log.i(TAG, "记录文件信息:\n" + RRDataList.toString());
		} catch (Exception e) {
			Log.e(TAG, "发送read record指令并解析响应 异常", e);
			throw e;
		}
	}
	/**
	 * 发送GPO指令，并解析响应
	 * 
	 * @param gpocom
	 *            GPO指令
	 * @throws Exception
	 */
	private void sendGPOData(byte[] gpocom) throws Exception {
		Log.v(TAG, "发送GPO指令");
		try {
			byte[] gpresponse = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(gpocom));

			String hexString = Utils.toHexStringNoBlank(gpresponse);
			Log.v(TAG, "GPO指令反馈内容:" + hexString);
			if (hexString.length() > 4) {
				Log.v(TAG, "GPO指令响应成功");
				AIP = hexString.substring(4, 8);
				AFL = hexString.substring(8);
				Log.v(TAG, "终端读取AIP和AFL,AIP=" + AIP + ",AFL=" + AFL);
			} else {
				// 返回6985的处理，卡片不支持该应用
				Log.v(TAG, "GPO指令响应失败");
				continueFlag = false;
			}
		} catch (Exception e) {
			Log.e(TAG, "发送GPO指令异常", e);
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
		Log.v(TAG, "根据PDOL数据包含内容组织GPO指令");

		StringBuffer gpodata = new StringBuffer();
		gpodata.append(getResources().getString(R.string.GPOCLA));
		gpodata.append(getResources().getString(R.string.GPOINS));
		gpodata.append(getResources().getString(R.string.GPOP1));
		gpodata.append(getResources().getString(R.string.GPOP2));
		String gpodatalen = "";
		String pdoldatastrlen = "";

		if (list != null) {
			StringBuffer datastr = new StringBuffer();
			String pd = null;
			for (int i = 0; i < list.size(); i++) {
				pd = list.get(i);
				// 终端交易属性
				if (getResources().getString(R.string.tag9F66).equals(pd)) {
					datastr.append(getResources().getString(
							R.string.tag9F66value));
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
					df = new DecimalFormat(pattern);
					EditText inamount = (EditText) findViewById(R.id.et_credit_for_load_inputmoney);
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
					dfdate = new SimpleDateFormat("yyMMdd");
					datastr.append(dfdate.format(new Date()));
					continue;
				}
				// 交易类型
				if (getResources().getString(R.string.tag9C).equals(pd)) {
					datastr.append(getResources()
							.getString(R.string.tag9Cvalue));
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
		Log.v(TAG, "GPO指令:" + gpodata.toString());
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
	private List<String> pasePdol() {
		Log.v(TAG, "解析PDOL数据");
		List<String> pdolcontext = null;
		if (PDOLMsg != null) {
			pdolcontext = new ArrayList<String>();
			String pdoltemp = Utils.toHexStringNoBlank(PDOLMsg);
			if (pdoltemp.contains(getResources().getString(R.string.tag9F66))) {
				Log.v(TAG, "PDOL数据包含9F66:终端交易属性");
				pdolcontext.add(getResources().getString(R.string.tag9F66));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7A))) {
				Log.v(TAG, "PDOL数据包含9F7A:电子现金终端支持指示器");
				pdolcontext.add(getResources().getString(R.string.tag9F7A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7B))) {
				Log.v(TAG, "PDOL数据包含9F7B:电子现金终端交易限额");
				pdolcontext.add(getResources().getString(R.string.tag9F7B));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F02))) {
				Log.v(TAG, "PDOL数据包含9F02:授权金额");
				pdolcontext.add(getResources().getString(R.string.tag9F02));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F03))) {
				Log.v(TAG, "PDOL数据包含9F03:其它金额");
				pdolcontext.add(getResources().getString(R.string.tag9F03));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F1A))) {
				Log.v(TAG, "PDOL数据包含9F1A:终端国家代码");
				pdolcontext.add(getResources().getString(R.string.tag9F1A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag95))) {
				Log.v(TAG, "PDOL数据包含95:终端验证结果");
				pdolcontext.add(getResources().getString(R.string.tag95));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag5F2A))) {
				Log.v(TAG, "PDOL数据包含5F2A:交易货币代码");
				pdolcontext.add(getResources().getString(R.string.tag5F2A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9A))) {
				Log.v(TAG, "PDOL数据包含9A:交易日期");
				pdolcontext.add(getResources().getString(R.string.tag9A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9C))) {
				Log.v(TAG, "PDOL数据包含9C:交易类型");
				pdolcontext.add(getResources().getString(R.string.tag9C));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F37))) {
				Log.v(TAG, "PDOL数据包含9F37:终端不可预知数据");
				pdolcontext.add(getResources().getString(R.string.tag9F37));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tagDF60))) {
				Log.v(TAG, "PDOL数据包含9F37:终端不可预知数据");
				pdolcontext.add(getResources().getString(R.string.tagDF60));
			}
		}
		return pdolcontext;
	}

	/**
	 * 重发select指令，获得PDOL数据
	 */
	private void reSendSelect() {
		Log.v(TAG, "重发select指令获得PDOL数据");
		try {
			byte[] fcibytes = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(Utils.bytesFromHexStringNoBlank(getResources()
							.getString(R.string.selectcmd))));

			if (fcibytes.length > 2) {
				Log.v(TAG, "重发select指令响应成功");
				Log.v(TAG, "select指令响应内容:" + Utils.toHexString(fcibytes));
				String str = Utils.toHexStringNoBlank(fcibytes);
				List<TLVEntity> list = Utils.getNodes(str);
				for (int i = 0; i < list.size(); i++) {
					if (getResources().getString(R.string.tagA5).equals(
							Utils.intToHexString(list.get(i).getTag()))) {
						List<TLVEntity> list1 = list.get(i).getNodes();
						for (int j = 0; j < list1.size(); j++) {
							if (getResources().getString(R.string.tag9F38)
									.equals(Utils.intToHexString(list1.get(j)
											.getTag()))) {
								PDOLMsg = Utils.bytesFromHexStringNoBlank(Utils
										.toHexString(list1.get(j).getValue(),
												0, list1.get(j).length));
								break;
							}
						}
					}
				}
				Log.v(TAG,
						"解析select指令响应获得PDOL数据:" + Utils.toHexString(PDOLMsg));
				Log.v(TAG, "重发select指令获得PDOL数据成功");
			} else {
				Log.v(TAG, "重发select指令响应失败，指令响应为空");
				continueFlag = false;
			}

		} catch (Exception e) {
			Log.e(TAG, "重发select指令异常", e);
			continueFlag = false;
			showDialog("提示", "初始化充值界面异常\n将发送错误报告。");
		}
	}

	/**
	 * 判断返回应用数据中是否有PDOL
	 * 
	 * @return boolean
	 */
	private boolean hasPDOL() {
		Log.v(TAG, "判断select指令响应中是否包含PDOL数据");
		boolean pdolFlag = false;
		try {
			String str = Utils.toHexStringNoBlank(FCIMsg);
			if ("9000".equals(str.substring(str.length() - 4))) {
				List<TLVEntity> list = Utils.getNodes(str);
				for (int i = 0; i < list.size(); i++) {
					if (getResources().getString(R.string.tagA5).equals(
							Utils.intToHexString(list.get(i).getTag()))) {
						List<TLVEntity> list1 = list.get(i).getNodes();
						for (int j = 0; j < list1.size(); j++) {
							if (getResources().getString(R.string.tag9F38)
									.equals(Utils.intToHexString(list1.get(j)
											.getTag()))) {
								PDOLMsg = Utils.bytesFromHexStringNoBlank(Utils
										.toHexString(list1.get(j).getValue(),
												0, list1.get(j).length));
								Log.v(
										TAG,
										"PDOL数据:"
												+ Utils.toHexStringNoBlank(PDOLMsg));
								break;
							}
						}
					}
				}
			} else if ("6A81".equals(str.substring(str.length() - 4))) {
				Log.v(TAG, "select指令响应结果为6A81，卡片被锁或命令不支持");
				continueFlag = false;
			} else if ("6A82".equals(str.substring(str.length() - 4))) {
				Log.v(TAG, "select指令响应结果为6A82，所选的文件未找到");
				continueFlag = false;
			} else if ("6283".equals(str.substring(str.length() - 4))) {
				Log.v(TAG, "select指令响应结果为6283，选择文件无效");
				continueFlag = false;
			} else {
				Log.v(TAG, "select指令响应失败，响应结果为:" + str);
			}

			if (PDOLMsg != null) {
				pdolFlag = true;
			} else {
				Log.v(TAG, "select指令响应中不包含PDOL数据");
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "解析PDOL数据失败", e);
			continueFlag = false;
			showDialog("提示", "初始化充值界面异常\n解析PDOL数据失败，将发送错误报告。");
		} catch (Exception e) {
			Log.e(TAG, "解析PDOL数据失败", e);
			continueFlag = false;
			showDialog("提示", "初始化充值界面异常\n解析PDOL数据失败，将发送错误报告。");
		}
		return pdolFlag;
	}

	/**
	 * 
	 * 方法描述:显示上传日志的对话框
	 */
	private void showDialog(String title, String context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLoad.this)
				.setTitle(title).setMessage(context);
		AlertDialog dialog = builder.show();
	}

	private Map<String, String> getIssuerData(String atc, String balance,
			String aam, String ac) throws Exception {
		byte[] key = ByteUtil
				.hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

		String pbocATC = new String(atc);//
		String pbocECBalance = new String(balance);//
		String pbocAAM = new String(aam);//
		String pbocAC = new String(ac);//

		Map<String, String> res = new HashMap<String, String>();
		// 生成分散数据
		String disperseData = "";
		for (int i = 0; i < 16 - pbocATC.length(); i++) {
			disperseData += "0";
		}
		disperseData += pbocATC;
		byte[] tempbyte = ByteUtil.hexStringToByteArray(pbocATC);
		for (int i = 0; i < tempbyte.length; i++) {
			tempbyte[i] = (byte) (tempbyte[i] ^ 0xFF);
		}
		String tempStr = ByteUtil.byteArrayToHexString(tempbyte);
		String disperseData2 = "";
		for (int i = 0; i < 16 - tempStr.length(); i++) {
			disperseData2 += "0";
		}
		disperseData2 += tempStr;
		// 拼接分散数据
		disperseData += disperseData2;
		System.out.println("密钥-->" + ByteUtil.byteArrayToHexString(key));
		System.out.println("分散数据-->" + disperseData);

		// 电子现金充值处理
		// 1.算出过程密钥
		byte[] processKey = CryptographyUtil.des3_ECB_encryption(key,
				ByteUtil.hexStringToByteArray(disperseData));
		System.out.println("过程密钥-->"
				+ ByteUtil.byteArrayToHexString(processKey));

		// 2.生成脚本MAC计算输入数据
		// 04DA9F790A 0012 E2F6ED390FDB4755 000000000104 800000
		// 固定值 ATC 应用密文 圈存后的金额 补位

		// 算圈存后的总金额

		// 按十进制运算
		int pbocECBalanceTemp = Integer.parseInt(pbocECBalance); // 电子现金余额
		int pbocAAMTemp = Integer.parseInt(pbocAAM);

		int countBalance = pbocECBalanceTemp + pbocAAMTemp;
		String countBalanceStr = String.valueOf(countBalance);
		String tempStr2 = "";
		for (int i = 0; i < 12 - countBalanceStr.length(); i++) {
			tempStr2 += "0";
		}
		tempStr2 += countBalanceStr;
		System.out.println("圈存后的余额-->" + tempStr2);
		String pbocECBalanceCount = tempStr2;
		String disperseData3 = "04DA9F790A" + pbocATC + pbocAC
				+ pbocECBalanceCount + "800000";
		System.out.println("算MAC数据-->" + disperseData3);
		byte[] retMac = CryptographyUtil.singleDesPlus3DES(processKey,
				ByteUtil.hexStringToByteArray(disperseData3),
				ByteUtil.hexStringToByteArray("0000000000000000"));
		System.out.println("\n计算出的MAC值-->"
				+ ByteUtil.byteArrayToHexString(retMac));
		if (retMac == null || retMac.length == 0) {
			throw new Exception("返回的MAC值为空");
		}
		String retMacStr = ByteUtil.byteArrayToHexString(retMac);
		String newRetMacStr = retMacStr.substring(0, 8);
		// 圈存脚本
		String retScript = "72199F1804000000018610" + "04DA9F790A"
				+ pbocECBalanceCount + newRetMacStr + "00";

		// 算出认证数据
		// arpc 计算
		// 取授权应答码（成功为3030） 6个字节0x00，和ARQC做异或运算
		// 3030000000000000
		// 应用密文（AC） <==> ARQC
		byte[] dataByte = ByteUtil.hexStringToByteArray("3030000000000000");
		byte[] arqcByte = ByteUtil.hexStringToByteArray(pbocAC);
		byte[] authData = new byte[arqcByte.length];
		for (int i = 0; i < dataByte.length; i++) {
			authData[i] = (byte) (dataByte[i] ^ arqcByte[i]);
		}

		byte[] issAuthData = CryptographyUtil.des3_ECB_encryption(processKey,
				authData);
		String retIssAuthDataStr = ByteUtil.byteArrayToHexString(issAuthData)
				+ "3030";

		res.put("IssAuthData", retIssAuthDataStr);
		System.out.println("认证数据-->" + retIssAuthDataStr);
		res.put("IssuerScript", retScript);
		System.out.println("圈存脚本-->" + retScript);

		return res;
	}
}
