package cn.z.ecash;

import cn.z.ecash.commn.ByteUtil;
import cn.z.ecash.commn.Utils;
import cn.z.ecash.nfc.PbocManager;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

public class DetailActivity extends Activity {

	private static final String LOGTAG = new String("DETAIL");
	private String detailinfo = null;
	private PbocManager pbocmanager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		if (savedInstanceState == null) {
		}

		TextView tvdetail = (TextView) findViewById(R.id.tv_detail);
		detailinfo = "=====PBOC Log=====\n";
		Log.i(LOGTAG, "【onCreate】:start");

		pbocmanager = PbocManager.getInstance();
		if (pbocmanager == null) {
			detailinfo += "PbocManager Err!!";
		}

		String fci = pbocmanager.SelectdefaultApplet();
		if (null == fci) {
			Log.i(LOGTAG, "【processcreditforload】:select err");
			detailinfo += "SelectdefaultApplet Err!!";
		}

		Log.i(LOGTAG, "【onCreate】:fci=" + fci);

		byte[] FCIMsg = Utils.bytesFromHexStringNoBlank(fci);

		String tradelogentry = pbocmanager
				.getPbocData(PbocManager.INDEX_TRADE_LOGENTRY);
		byte[] tradelogentrybytes = ByteUtil
				.hexStringToByteArray(tradelogentry);
		byte getTradelogP2 = (byte) ((tradelogentrybytes[0] << 3) | 0x04);
		int tradelogrecordmaxnum = tradelogentrybytes[1];
		Log.i(LOGTAG,
				"【onCreate】:tradelogentry=" + tradelogentry + "   \nP2=%2x"
						+ String.format("%02x", getTradelogP2)
						+ "   \nP1 Max=%2x"
						+ String.format("%02x", tradelogrecordmaxnum));

		String loadlogentry = pbocmanager
				.getPbocData(PbocManager.INDEX_LOAD_LOGENTRY);
		Log.i(LOGTAG, "【onCreate】:loadlogentry=" + loadlogentry);

		String PBOCAID = pbocmanager.getAID();

		String tagtradelogdol = pbocmanager.getTag(new String("9F4F"));
		Log.i(LOGTAG, "【onCreate】:Tag9F4F=" + tagtradelogdol);

		String tagloadlogdol = pbocmanager.getTag(new String("DF4F"));
		Log.i(LOGTAG, "【onCreate】:tagDF4F=" + tagloadlogdol);

		detailinfo += "PBOCAID=" + PBOCAID + "\n";
		detailinfo += "fci=" + fci + "\n";
		detailinfo += "Tag9F4D=" + tradelogentry + "   \nP2="
				+ String.format("%02x", getTradelogP2) + "   \nP1 Max="
				+ String.format("%02x", tradelogrecordmaxnum) + "\n";
		detailinfo += "TagDF4D=" + loadlogentry + "\n";
		detailinfo += "Tag9F4F=" + tagtradelogdol + "\n";
		detailinfo += "tagDF4F=" + tagloadlogdol + "\n";


		tvdetail.setText(detailinfo);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
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
			View rootView = inflater.inflate(R.layout.fragment_detail,
					container, false);
			return rootView;
		}
	}
}
