package cn.z.ecash;

import java.util.HashMap;
import java.util.Map;

import cn.z.ecash.nfc.CardManager;
import cn.z.ecash.nfc.PbocManager;
import cn.z.ecash.nfc.NfcUtil;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private static final String LOGTAG = new String("MAIN");

	private final static int REQUEST_CODE = 1;
	TextView tvShowDebuginfo;

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;

	public PbocManager pbocmanager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Resources res = getResources();
		this.res = res;

		Button btnGetBalance = (Button) findViewById(R.id.btn_get_balance);
		Button btnGetDetail = (Button) findViewById(R.id.btn_get_detail);
		Button btnGetCardNumber = (Button) findViewById(R.id.btn_get_cardnumber);
		Button btnCreditForLoad = (Button) findViewById(R.id.btn_credit_for_load);
		Button btnGetATC = (Button) findViewById(R.id.btn_get_ATC);
		Button btnQuickPass = (Button) findViewById(R.id.bt_quickpass);

		tvShowDebuginfo = (TextView) findViewById(R.id.tv_debugshow);

		btnGetBalance.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashGetBalance();
			}
		});

		btnGetDetail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashGetDetail();
			}
		});
		btnGetCardNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashGetCardNumber();
			}
		});

		btnCreditForLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashCreditForLoad();
			}
		});

		btnGetATC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashGetATC();
			}
		});

		btnQuickPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				eCashQuickPass();
			}
		});

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		onNewIntent(getIntent());

	}

	@Override
	protected void onNewIntent(Intent intent) {
		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (p == null) {
			Log.i(LOGTAG, "【onNewIntent】:Parcelable == null");
			return;
		}
		Log.i(LOGTAG, "【onNewIntent】:Parcelable != null");
		PbocManager.clearInstance();
		Log.i(LOGTAG, "【onNewIntent】:clear PbocManager instance");
		pbocmanager = PbocManager.getInstance(p, res);
		Log.i(LOGTAG, "【onNewIntent】:PbocManager.getInstance" + pbocmanager);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	void eCashGetBalance() {
		Log.i(LOGTAG, "【eCashGetBalance】:start");
		PbocManager pbocbalance = PbocManager.getInstance();
		if (null == pbocbalance) {
			Log.i(LOGTAG, "【eCashGetBalance】:no card");
			return;
		}
		if (null == pbocbalance.SelectdefaultApplet()) {
			Log.i(LOGTAG, "【eCashGetBalance】:select card err");
			return;
		}
		tvShowDebuginfo.setText("---Card info---\nbalance="
				+ pbocbalance.getBalance() + "元");
	}

	void eCashGetDetail() {
		// tvShowDebuginfo.setText("---debug info---\nget detail:\n"+pbocm.getLog("log"));
	}

	void eCashGetCardNumber() {
		Log.i(LOGTAG, "【eCashGetCardNumber】:start");
		PbocManager pbocbalance = PbocManager.getInstance();
		if (null == pbocbalance) {
			Log.i(LOGTAG, "【eCashGetCardNumber】:no card");
			return;
		}
		if (null == pbocbalance.SelectdefaultApplet()) {
			Log.i(LOGTAG, "【eCashGetCardNumber】:select card err");
			return;
		}
		String strn = pbocmanager.sendAPDU("00B2011400");
		byte[] tempbytes = NfcUtil.hexStringToByteArray(strn);
		int toff = 0;
		if ((byte) 0x70 == tempbytes[toff++]) {
			if ((byte) 0x81 == tempbytes[toff++]) {
				toff++;
			}
			toff = NfcUtil.findValueOffByTag((short) 0x5A, tempbytes,
					(short) toff, (short) tempbytes[toff - 1]);
			if (toff < 0) {
				strn = pbocmanager.sendAPDU("00B2011C00");
				tempbytes = NfcUtil.hexStringToByteArray(strn);
				if ((byte) 0x70 == tempbytes[toff++]) {
					if ((byte) 0x81 == tempbytes[toff++]) {
						toff++;
					}
					toff = NfcUtil.findValueOffByTag((short) 0x5A, tempbytes,
							(short) toff, (short) tempbytes[toff - 1]);
					if (toff < 0) {
						toff = -1;
					}
				}
			}
		}
		if(toff < 0){
			strn = null;
		}else{
			strn = NfcUtil.toHexString(tempbytes, toff, tempbytes[toff - 1]);
			int numberend = strn.indexOf("F");
			if(numberend > 0){
				strn = strn.substring(0,numberend);
			}
		}
		
		tvShowDebuginfo.setText("---Card info---\ncard number:\n"
				+ strn);
	}

	void eCashGetATC() {
		Log.i(LOGTAG, "【eCashGetATC】:start");
		pbocmanager = PbocManager.getInstance();
		if (null == pbocmanager) {
			Log.i(LOGTAG, "【eCashGetBalance】:no card");
			return;
		}
		if (null == pbocmanager.SelectdefaultApplet()) {
			Log.i(LOGTAG, "【eCashGetATC】:select card err");
			return;
		}
		tvShowDebuginfo
				.setText("---Card info---\nATC=" + pbocmanager.getTag("9F36"));
		Log.i(LOGTAG, "【eCashGetATC】:end");
	}

	void eCashQuickPass() {
		tvShowDebuginfo.setText("---Card info---\nQuick Pass");
		Intent nIntent = new Intent(MainActivity.this, QuickPassActivity.class);
		startActivity(nIntent);

	}

	void eCashCreditForLoad() {
		Bundle loadbdl = new Bundle();
		loadbdl.putString("showstring", "credit for load info");

		Intent loadint = new Intent(this, ActivityLoad.class);
		loadint.putExtras(loadbdl);
		// startActivity(loadint);
		startActivityForResult(loadint, REQUEST_CODE);//

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE) {
			if (resultCode == REQUEST_CODE) {
				Bundle tbdl = data.getExtras();
				String inputloamoney = tbdl.getString("inputloamoney");
				tvShowDebuginfo.setText("---Card info---\ninput money\n"
						+ inputloamoney);
			}
		}

	}

}
