/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package cn.z.ecash.nfc;

import java.util.Map;

import cn.z.ecash.nfc.pboc.PbocCard;
import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.util.Log;

@SuppressLint("NewApi") public final class PbocManager {
	private final static byte[] PPSE = { 
		(byte) '2',(byte) 'P', (byte) 'A', (byte) 'Y',(byte) '.', 
		(byte) 'S', (byte) 'Y', (byte) 'S', (byte) '.',
		(byte) 'D', (byte) 'D', (byte) 'F',(byte) '0', (byte) '1'  };
	protected final static byte[] DFI_MF = { (byte) 0x3F, (byte) 0x00 };
	protected final static byte[] DFI_EP = { (byte) 0x10, (byte) 0x01 };

	protected final static byte[] DFN_PSE = { (byte) '1', (byte) 'P',
			(byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
			(byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F',
			(byte) '0', (byte) '1', };

	protected final static byte[] DFN_PXX = { (byte) 'P' };

	protected final static int MAX_LOG = 10;
	protected final static int SFI_EXTRA = 21;
	protected final static int SFI_LOG = 24;

	protected final static byte TRANS_CSU = 6;
	protected final static byte TRANS_CSU_CPX = 9;

	protected String name;
	protected String id;
	protected String serl;
	protected String version;
	protected String date;
	protected String count;
	protected String cash;
	protected String log;

	public static String[][] TECHLISTS;
	public static IntentFilter[] FILTERS;
	
	private byte[] DFAID = null;
	private byte LOGSFI = 0;
	private Iso7816.Tag TAG7816;


	static {
		try {
			TECHLISTS = new String[][] { { IsoDep.class.getName() },
					{ NfcV.class.getName() }, { NfcF.class.getName() }, };

			FILTERS = new IntentFilter[] { new IntentFilter(
					NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
		} catch (Exception e) {
		}
	}
	public boolean init(Parcelable parcelable, Resources res){
		final Tag tag = (Tag) parcelable;
		if(tag == null){
			return false ;
		}
		final IsoDep isodep = IsoDep.get(tag);
		if (isodep == null) {
			return false;
		}
		
		TAG7816 = new Iso7816.Tag(isodep);
		TAG7816.connect();
		return true;
	}
	
	public boolean SelectCard(Parcelable parcelable){
		return SelectCard(parcelable,null);
	}
	public boolean SelectCard(Parcelable parcelable, Resources res){
		final Tag tag = (Tag) parcelable;
		if(tag == null){
			return false ;
		}
		final IsoDep isodep = IsoDep.get(tag);
		if (isodep == null) {
			return false;
		}
		
		TAG7816 = new Iso7816.Tag(isodep);
		TAG7816.connect();
		byte[] tbyte = null;
		int toff = 0;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  false;
				
				short FCIdefoff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  false;
				
				short Contentoff = Util.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  false;
				
				short DFAIDoff = Util.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  false;
				
				short APPlaboff = Util.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  false;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return false;
			}
			Log.i("PBOCM", "【AID】:" + Util.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i("ECASH", "【select Main Application】:" + selectdffci.toString());

		if (selectdffci.isOkey()) {
			tbyte = selectdffci.getBytes();
			if(tbyte == null){
				return false;
			}
			toff = 0;
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				toff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				int toffA5 = toff;

				//PDOL
				toff = Util.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				
				toff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  false;
				
				toff = Util.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				LOGSFI = tbyte[toff];
			}
			Log.i("ECASH", "【LOGSFI】:" + LOGSFI);
		}

		return true;
	}
	
	public boolean Select(){
		byte[] tbyte = null;
		int toff = 0;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  false;
				
				short FCIdefoff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  false;
				
				short Contentoff = Util.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  false;
				
				short DFAIDoff = Util.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  false;
				
				short APPlaboff = Util.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  false;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return false;
			}
			Log.i("ECASH", "【AID】:" + Util.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i("ECASH", "【select Main Application】:" + selectdffci.toString());

		if (selectdffci.isOkey()) {
			tbyte = selectdffci.getBytes();
			if(tbyte == null){
				return false;
			}
			toff = 0;
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				toff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				int toffA5 = toff;

				//PDOL
				toff = Util.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				
				toff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  false;
				
				toff = Util.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				LOGSFI = tbyte[toff];
			}
			Log.i("ECASH", "【LOGSFI】:" + LOGSFI);
		}

		return true;
	}
	
	
	public String SelectdefaultApplet(){
		byte[] tbyte = null;
		int toff = 0;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  null;
				
				short FCIdefoff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  null;
				
				short Contentoff = Util.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  null;
				
				short DFAIDoff = Util.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  null;
				
				short APPlaboff = Util.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  null;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return null;
			}
			Log.i("ECASH", "【SelectdefaultApplet.AID】:" + Util.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i("ECASH", "【SelectdefaultApplet.FCI】:" + selectdffci.toString());

		if (!selectdffci.isOkey()) {
			return null;
		}
		return selectdffci.toString();
	}
	
	
	public String CreditForLoad(Parcelable parcelable){
		return CreditForLoad(parcelable,null);
	}
	public String CreditForLoad(Parcelable parcelable, Resources res){
		final Tag tag = (Tag) parcelable;
		if(tag == null){
			return null ;
		}
		final IsoDep isodep = IsoDep.get(tag);
		if (isodep == null) {
			return null;
		}
		
		
		TAG7816 = new Iso7816.Tag(isodep);
		TAG7816.connect();
		byte[] tbyte = null;
		int toff = 0;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  null;
				
				short FCIdefoff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  null;
				
				short Contentoff = Util.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  null;
				
				short DFAIDoff = Util.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  null;
				
				short APPlaboff = Util.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  null;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return null;
			}
			Log.i("PBOCM", "【AID】:" + Util.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i("ECASH", "【select Main Application】:" + selectdffci.toString());

		if (selectdffci.isOkey()) {
			tbyte = selectdffci.getBytes();
			if(tbyte == null){
				return null;
			}
			toff = 0;
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				toff = Util.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				int toffA5 = toff;

				//PDOL
				toff = Util.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				
				
				toff = Util.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  null;
				
				toff = Util.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				
				LOGSFI = tbyte[toff];
			}
			Log.i("ECASH", "【LOGSFI】:" + LOGSFI);
		}
		return null;
	}

	public String sendAPDU(String cmd){
		Iso7816.Response res = TAG7816.sendCmd(cmd);

		return res.toString();
	}
	
	public String sendAPDU(byte[] cmd){
		Iso7816.Response res = TAG7816.sendCmd(cmd);

		return res.toString();
	}
	
	public String getBalance() {
		String strbalance;
		Iso7816.Response resbalance = TAG7816.sendCmd("80CA9F7900");
		if (!resbalance.isOkey() || resbalance.size() < 4) {
			return null;
		} 

		int n = Util.BCDtoInt(resbalance.getBytes(), 5, 4);
		strbalance = Util.toAmountString(n / 100.0f);
		return strbalance;
	}
}
