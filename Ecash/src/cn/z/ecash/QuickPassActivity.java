package cn.z.ecash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
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
import cn.z.ecash.commn.TLVEntity;
import cn.z.ecash.commn.Utils;
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

//	private static String CAPublickeyExp = new String("03");
//	private static String CAPublickeyModuls = new String(
//			"00EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5");

	/** CA公钥列表，RID,index,exp,moduls. */
	private static List<String[]> CAPublicKeyList = new ArrayList<String[]>();
	static {
		CAPublicKeyList.add(new String[]{"A000000333","02","03","A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57"});
		CAPublicKeyList.add(new String[]{"A000000333","03","03","B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33D"});
		CAPublicKeyList.add(new String[]{"A000000333","06","03","EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5"});
		CAPublicKeyList.add(new String[]{"A000000333","08","03","B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF"});
		CAPublicKeyList.add(new String[]{"A000000333","09","03","EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5"});
		CAPublicKeyList.add(new String[]{"A000000333","0A","03","B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF"});	
		CAPublicKeyList.add(new String[]{"A000000333","0B","03","CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157"});
		CAPublicKeyList.add(new String[]{"A000000333","0F","03","BE7AF1AE9FE3E22FE5422F7A22F188020717A2A629D19505F7697916954C239C313E715965265D0262E36EB165AF71C88D331D55C903D6F9E12DAC99C0741F946E93AA9E6BE3D7191AF49BFC4AF9AC282B43AFEDD3A6CA625228C26423E0C4AAF27BBF738A6A7F4816F68494B1EA89816BD7F3E5686881EADF680E282B48B40199B13E538845B6F0D3D072A931190C28190B887EB97E5FB2B410497158DA1810592B26BE4E40576882984B65426466C7"});
		CAPublicKeyList.add(new String[]{"A000000333","19","010010","CC867A69A848904F86818A50EE87390FB8E095BC93C9F16D06765B4A0E10FF7B23484BC92B24DF657CD90A98CFF4CE82475E4C32E0B3B7F8A45E51FA53E6E3E33B32CBE7445997F41C154550B30B1B7BB748470D54939B23A9589E6B23180A2737168AE9FF59287F3F60026A5F13F5DFC232D02DF60CD865734872DF21ECCD55"});
		CAPublicKeyList.add(new String[]{"A000000333","61","03","950CD699405D0C900A33675CCB91EE2C7488FF638CFB38AE4E25104E6ED731F5A566D290BBED5400DE0DFF3096FB1FDF261879A6AFE047F283C8B5C34AB9BD07051655DDE278E0F96760E7E8C0FD38A0BC1510E26B27FD58B30A2C06074D47C8883C8B7009587A86AD230D6A4F40DA4ACC555DB8525398B0A28CE5A1089EACAFC3B4C43CFA50833D442E048B3B04FBD69AE959CA9EC126D65D68BB0B395C624C957ECA48B79C41695EDA49AECD003CD363C7592AC25BAB009EBA6F844D8910A8A856FDB99559B521C4920456C6E986B2D7C6C2255797EA29DCA31B4C39E583CD437CC786E402529E3CA2E9BEE0394B378C13FC523CCB2E17"});

		CAPublicKeyList.add(new String[]{"A000000003","01","03","E458C3CFE560BCEF73CBD23A106894E8914497A964E4B964922683BDE1A9B4689C5EA0816F30047CBF23A95E14A6C07A0849914D24775B7C4BEFEDF631BEA5AB21B90DA1701D48C8CCD8BA45E14FA0390BE50C8EE97A2BB7C846C5563DBF00E91D219ED7D1F5DE80011308AAB61F564B82742ED977A29D7FD02BDD1D4CA953F5"});
		CAPublicKeyList.add(new String[]{"A000000003","07","03","A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725F"});
		CAPublicKeyList.add(new String[]{"A000000003","08","03","E2038DE65ADF4562502349BC914317C8D3A50984481834BD159ACCB018EF78367F95DF56B52BE58BBD57483D019391F8E6BEDDF83862E8BA76DB3C379C1A493D26EA46CE17262866145CF848D935F198EADE2344D92B5321249DF313B299AEEACC3EBEB9654F7EA473C38FE571DAD7DA42C2B0C8C9D7E788228A55B6E41E5D1F5D24942B788ABD4D153AB5D583F9859AC045AF00707413A93ED5CF0806222F801D815BE081A5694F19C24500802CFEEB"});
		CAPublicKeyList.add(new String[]{"A000000003","09","03","C132F436477A59302E885646102D913EC86A95DD5D0A56F625F472B67F52179BC8BD258A7CD43EF1720AC0065519E3FFCECC26F978EDF9FB8C6ECDF145FDCC697D6B72562FA2E0418B2B80A038D0DC3B769EB027484087CCE6652488D2B3816742AC9C2355B17411C47EACDD7467566B302F512806E331FAD964BF000169F641"});
		CAPublicKeyList.add(new String[]{"A000000003","51","03","DB5FA29D1FDA8C1634B04DCCFF148ABEE63C772035C79851D3512107586E02A917F7C7E885E7C4A7D529710A145334CE67DC412CB1597B77AA2543B98D19CF2CB80C522BDBEA0F1B113FA2C86216C8C610A2D58F29CF3355CEB1BD3EF410D1EDD1F7AE0F16897979DE28C6EF293E0A19282BD1D793F1331523FC71A228800468C01A3653D14C6B4851A5C029478E757F"});
		CAPublicKeyList.add(new String[]{"A000000003","94","03","D1BE39615F395AC9337E3307AA5A7AC35EAE0036BF20B92F9A45D190B2F4616ABF9D340CBF5FBB3A2B94BD8F2F977C0A10B90E59D4201AA32669E8CBE753F536119DF4FB5E63CED87F1153CE914B124F3E6B648CD5C97655F7AB4DF62607C95DA50517AB8BE3836672D1C71BCDE9BA7293FF3482F124F86691130AB08177B02F459C025A1F3DFFE0884CE78122542EA1C8EA092B552B586907C83AD65E0C6F91A400E485E11192AA4C171C5A1EF56381F4D091CC7EF6BD8604CBC4C74D5D77FFA07B641D53998CDB5C21B7BC65E082A6513F424A4B252E0D77FA4056986A0AB0CDA6155ED9A883C69CC2992D49ECBD4797DD2864FFC96B8D"});
		CAPublicKeyList.add(new String[]{"A000000003","95","03","BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B"});
		CAPublicKeyList.add(new String[]{"A000000003","96","03","B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D"});
		CAPublicKeyList.add(new String[]{"A000000003","97","03","B2E8B5497CF160A455C7FC53644899E2D53226B768B04A355BC824D09B1F824DB63FE3AF018AB382C78C48F4CE37FA5F63C2A2FDC4798E683456B001D77AC374C81CF6B0D0ADA85840D2633F2AEF1073CAD9E0B53AECFE14D47B1E92E348866F1EC84D3DF2C303A50A1E1E09E33A2B55C1A81B9A0A26BD89E93574AEE125721AF1377F1A0676F83F698D4113478A9B148A9E6BEF6F3EC952F4C4C64B2ACF38AEE4AE546B3E6128DB419511D197387CA36C5BF977735D9E06D652800C8446A6F53F44B4F32C6969011C92808FC93A90340A6B68440FCB6158FC13A819A0F3E49AE53EBB79AC92AE69ADD1EBE1ADADD0E29C0DF141"});
		CAPublicKeyList.add(new String[]{"A000000003","99","03","AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777"});
	}
	
	
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;
	private PbocManager pbocmanager = null;
	private String PBOCAID = null;
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
		PBOCAID = pbocmanager.getAID();
		
		byte[] fcibytes = Utils.bytesFromHexStringNoBlank(fci);
		int pdoloff = getPBOCPDOL(fcibytes);
		byte[] PDOLbytes = new byte[fcibytes[pdoloff - 1]];
		System.arraycopy(fcibytes, pdoloff, PDOLbytes, 0, PDOLbytes.length);
		Log.i(LOGTAG, "【processforquickpass】:PDOLoff-->" + pdoloff
				+ "\nPDOL-->" + Utils.toHexString(PDOLbytes));

		List<String> PDOLlist = pasePdol(PDOLbytes);
		byte[] GPOcmd = getGPOData(PDOLlist);
		Log.i(LOGTAG,
				"【processforquickpass】:GPOcmd-->" + Utils.toHexString(GPOcmd));

		String GPOres = sendGPOData(GPOcmd);
		String strAIP = "";
		String strAFL = "";
		if ("77".equals(GPOres.substring(0, 2))) {
			byte[] gporesbytes = Utils.HexString2Bytes(GPOres);
			toff = 1;
			if ((byte) 0x81 == gporesbytes[toff++]) {
				toff++;
			}
			short aipoff = Utils.findValueOffByTag((short) 0x82, gporesbytes,
					(short) toff, (short) gporesbytes[toff - 1]);
			if (aipoff < 0) {
				Log.i(LOGTAG, "【processforquickpass】:AIP not found");
				return purchaseresult;
			}
			strAIP = GPOres.substring(aipoff * 2, aipoff * 2 + 4);
			short afloff = Utils.findValueOffByTag((short) 0x94, gporesbytes,
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
		showDialog("消费成功", "当前余额:" + balance + "元.");
		return purchaseresult;
	}

	private void RSAAuth(String pAIP) throws Exception {
		String CAindex = RRDataList.get(getResources().getString(R.string.tag8F));
		if(CAindex == null){
			Log.i(LOGTAG, "【RSAAuth】:记录文件错误，没有CA公钥索引");
			return;
		}
		String CAPublickeyModuls = getCAModuls(PBOCAID.substring(0,10), CAindex);
		String CAPublickeyExp = getCAExp(PBOCAID.substring(0,10), CAindex);
		Log.i(LOGTAG, "【RSAAuth】:CA公钥模长\n" + CAPublickeyModuls
				+"CAg公钥指数"+CAPublickeyExp);

		
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
					+ "\n实际值:" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:发卡行公钥证书Hash检验成功\n期望值:" + IPKHash_Card
				+ "\n实际值:" + tmpHash);
		
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
					+ "\n实际值:" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:IC卡公钥证书Hash检验成功\n期望值:" + ICCPKHash_Card
				+ "\n实际值:" + tmpHash);
		
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
					+ "\n实际值:" + tmpHash);
			return;
		}
		Log.i(LOGTAG, "【RSAAuth】:DDA签名Hash检验成功\n期望值:" + DDAHash_Card
				+ "\n实际值:" + tmpHash);
	}

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
		Log.v(TAG, "发送GPO指令");
		try {
			byte[] returnMsg2 = Utils.bytesFromHexStringNoBlank(pbocmanager
					.sendAPDU(gpocom));
			if (returnMsg2 == null)
				return null;
			String hexString = Utils.toHexStringNoBlank(returnMsg2);
			Log.v(TAG, "GPO指令反馈内容:" + hexString);
			return hexString;
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
	private List<String> pasePdol(byte[] pdolmsg) {
		Log.i(LOGTAG, "解析PDOL数据");
		List<String> pdolcontext = null;
		if (pdolmsg != null) {
			pdolcontext = new ArrayList<String>();
			String pdoltemp = Utils.toHexStringNoBlank(pdolmsg);
			if (pdoltemp.contains(getResources().getString(R.string.tag9F66))) {
				Log.i(LOGTAG, "PDOL数据包含9F66:终端交易属性");
				pdolcontext.add(getResources().getString(R.string.tag9F66));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7A))) {
				Log.i(LOGTAG, "PDOL数据包含9F7A:电子现金终端支持指示器");
				pdolcontext.add(getResources().getString(R.string.tag9F7A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F7B))) {
				Log.i(LOGTAG, "PDOL数据包含9F7B:电子现金终端交易限额");
				pdolcontext.add(getResources().getString(R.string.tag9F7B));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F02))) {
				Log.i(LOGTAG, "PDOL数据包含9F02:授权金额");
				pdolcontext.add(getResources().getString(R.string.tag9F02));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F03))) {
				Log.i(LOGTAG, "PDOL数据包含9F03:其它金额");
				pdolcontext.add(getResources().getString(R.string.tag9F03));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F1A))) {
				Log.i(LOGTAG, "PDOL数据包含9F1A:终端国家代码");
				pdolcontext.add(getResources().getString(R.string.tag9F1A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag95))) {
				Log.i(LOGTAG, "PDOL数据包含95:终端验证结果");
				pdolcontext.add(getResources().getString(R.string.tag95));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag5F2A))) {
				Log.i(LOGTAG, "PDOL数据包含5F2A:交易货币代码");
				pdolcontext.add(getResources().getString(R.string.tag5F2A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9A))) {
				Log.i(LOGTAG, "PDOL数据包含9A:交易日期");
				pdolcontext.add(getResources().getString(R.string.tag9A));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9C))) {
				Log.i(LOGTAG, "PDOL数据包含9C:交易类型");
				pdolcontext.add(getResources().getString(R.string.tag9C));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tag9F37))) {
				Log.i(LOGTAG, "PDOL数据包含9F37:终端不可预知数据");
				pdolcontext.add(getResources().getString(R.string.tag9F37));
			}
			if (pdoltemp.contains(getResources().getString(R.string.tagDF60))) {
				Log.i(LOGTAG, "PDOL数据包含DF60:CAPP 交易指示位");
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
			toff = Utils.findValueOffByTag((short) 0xA5, tbyte, (short) toff,
					(short) tbyte[toff - 1]);
			if (toff < 0)
				return -1;
			int toffA5 = toff;

			// PDOL
			toff = Utils.findValueOffByTag((short) 0x9F38, tbyte,
					(short) toff, (short) tbyte[toffA5 - 1]);
			if (toff < 0)
				return -1;
			resoff = toff;

			toff = Utils.findValueOffByTag((short) 0xBF0C, tbyte,
					(short) toffA5, (short) tbyte[toffA5 - 1]);
			if (toff < 0)
				return -1;
			int BF0Coff = toff;

			toff = Utils.findValueOffByTag((short) 0x9F4D, tbyte,
					(short) toff, (short) tbyte[BF0Coff - 1]);
			if (toff < 0)
				return -1;
		}
		return resoff;
	}

	/**
	 * 
	 * 方法描述:
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

		byte[] pubkeymoduls = ByteUtil.hexStringToByteArray(pkeymoduls);
		byte[] pubkeyexp = ByteUtil.hexStringToByteArray(pkeyexp);
		
		if((pubkeymoduls[0] & 0x80) == 0x80){
			pubkeymoduls = ByteUtil.hexStringToByteArray("00"+pkeymoduls);
		}
		if((pubkeyexp[0] & 0x80) == 0x80){
			pubkeyexp = ByteUtil.hexStringToByteArray("00"+pkeyexp);
		}

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
	
	private static String getCAModuls(String prid,String pcaindex){
		String[] tarray;
		if(CAPublicKeyList == null )
			return null;
		int sum = CAPublicKeyList.size();
		if(sum == 0)
			return null;
		for(int i=0;i<sum;i++){
			tarray = CAPublicKeyList.get(i);
			if((tarray[0].equals(prid)) && (tarray[1].equals(pcaindex))){
				return tarray[3];
			}
		}
		return null;
	}
	
	private static String getCAExp(String prid,String pcaindex){
		String[] tarray;
		if(CAPublicKeyList == null )
			return null;
		int sum = CAPublicKeyList.size();
		if(sum == 0)
			return null;
		for(int i=0;i<sum;i++){
			tarray = CAPublicKeyList.get(i);
			if((tarray[0].equals(prid)) && (tarray[1].equals(pcaindex))){
				return tarray[2];
			}
		}
		return null;
	}
}
