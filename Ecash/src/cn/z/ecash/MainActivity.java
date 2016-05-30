package cn.z.ecash;

import java.util.HashMap;
import java.util.Map;

import cn.z.ecash.nfc.CardManager;
import cn.z.ecash.nfc.PbocManager;
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

@SuppressLint("NewApi") public class MainActivity extends Activity  {
	private final static int REQUEST_CODE = 1;
	TextView tvShowDebuginfo;
	
	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private Resources res;
	Map<String,Object> carddata = new HashMap<String, Object>();
	
	public static PbocManager pbocm = new PbocManager();


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
		
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		onNewIntent(getIntent());
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("MAIN", "【onResume】:start onResume()");

		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardManager.FILTERS, CardManager.TECHLISTS);

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
	@Override
	protected void onNewIntent(Intent intent) {
		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(p == null){
			Log.i("MAIN", "【onNewIntent】:Parcelable == null");
			return ;
		}
		Log.i("MAIN", "【onNewIntent1】:");
//		carddata = CardManager.read(p, res);
		pbocm.init(p, res);
		Log.i("MAIN", "【onNewIntent2】:");
	}
	protected boolean selectcard(Intent intent) {
		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(p == null){
			Log.i("MAIN", "【onNewIntent】:Parcelable == null");
			return false;
		}
		Log.i("MAIN", "【onNewIntent1】:");
		if(!pbocm.SelectCard(p, res)){
			Log.i("MAIN", "【onNewIntent2】:select err");
			return false;
		}
		Log.i("MAIN", "【onNewIntent2】:");
		return true;
	}
	
	void eCashGetBalance() {
		Log.i("MAIN", "【Click button get balance】:");
		if(!selectcard(getIntent())){
			Log.i("MAIN", "【Click button get balance】:select card err");
			return ;
		}
		tvShowDebuginfo.setText("---debug info---\nbalance="+pbocm.getBalance()+"元");
	}

	void eCashGetDetail() {
		tvShowDebuginfo.setText("---debug info---\nget detail:\n"+carddata.get("log"));
	}

	void eCashGetCardNumber() {
		tvShowDebuginfo.setText("---debug info---\nget card number:\n"+carddata.get("serl"));
	}

	void eCashGetATC() {
		tvShowDebuginfo.setText("---debug info---\nget ATC="+carddata.get("count"));
	}

	void eCashCreditForLoad() {
//		final Parcelable p = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
//		if(p == null){
//			Log.i("MAIN", "【onNewIntent】:Parcelable == null");
//			return ;
//		}
//		pbocm.init(p, res);
//		Log.i("MAIN", "【INIT】:done");
		
		Bundle loadbdl = new Bundle();
		loadbdl.putString("showstring", "credit for load 3413435");

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
				tvShowDebuginfo.setText("---debug info---\ninput money\n"
						+ inputloamoney);
			}
		}

	}

}
