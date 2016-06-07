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
import cn.z.ecash.commn.*;
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
	private final static String LOGTAG = new String("PBOCM");
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
	private Iso7816.Tag TAG7816 = null;
	
	private static PbocManager pbocinstance = null; 


	static {
		try {
			TECHLISTS = new String[][] { { IsoDep.class.getName() },
					{ NfcV.class.getName() }, { NfcF.class.getName() }, };

			FILTERS = new IntentFilter[] { new IntentFilter(
					NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
		} catch (Exception e) {
		}
	}
	
	private PbocManager(){
		
	}
	public static PbocManager getInstance(Parcelable parcelable, Resources res){
		if(pbocinstance != null)
			return pbocinstance;
		pbocinstance = new PbocManager();
		final Tag tag = (Tag) parcelable;
		if(tag == null){
			return null ;
		}
		final IsoDep isodep = IsoDep.get(tag);
		if (isodep == null) {
			return null;
		}
		
		pbocinstance.TAG7816 = new Iso7816.Tag(isodep);
		pbocinstance.TAG7816.connect();
		return pbocinstance;
	}
	
	public static PbocManager getInstance(Parcelable parcelable){
		if(pbocinstance != null)
			return pbocinstance;
		pbocinstance = new PbocManager();
		final Tag tag = (Tag) parcelable;
		if(tag == null){
			return null ;
		}
		final IsoDep isodep = IsoDep.get(tag);
		if (isodep == null) {
			return null;
		}
		
		pbocinstance.TAG7816 = new Iso7816.Tag(isodep);
		pbocinstance.TAG7816.connect();
		return pbocinstance;
	}
	
	public static PbocManager getInstance(){
		if(pbocinstance != null)
			return pbocinstance;
		else
			return null;
	}
	
	public static void clearInstance(){
		if(pbocinstance == null)
			return ;
		pbocinstance.TAG7816 = null;
		pbocinstance  = null;
		return  ;
	}
	public String getAID(){
		if(DFAID == null)
			return null;
		else
			return Utils.toHexString(DFAID, 0, DFAID.length);
	}
	public boolean SelectCard(){
		byte[] tbyte = null;
		int toff = 0;
		
		if(TAG7816 == null)
			return false;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  false;
				
				short FCIdefoff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  false;
				
				short Contentoff = Utils.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  false;
				
				short DFAIDoff = Utils.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  false;
				
				short APPlaboff = Utils.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  false;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return false;
			}
			Log.i(LOGTAG, "【AID】:" + Utils.toHexString(DFAID, 0, DFAID.length));
		}
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
				short FCIoff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  false;
				
				short FCIdefoff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  false;
				
				short Contentoff = Utils.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  false;
				
				short DFAIDoff = Utils.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  false;
				
				short APPlaboff = Utils.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  false;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return false;
			}
			Log.i(LOGTAG, "【AID】:" + Utils.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i(LOGTAG, "【select Main Application】:" + selectdffci.toString());

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
				toff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				int toffA5 = toff;

				//PDOL
				toff = Utils.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				
				toff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  false;
				
				toff = Utils.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				LOGSFI = tbyte[toff];
			}
			Log.i(LOGTAG, "【LOGSFI】:" + LOGSFI);
		}

		return true;
	}
	
	public boolean Select(){
		byte[] tbyte = null;
		int toff = 0;
		if(TAG7816 == null)
			return false;
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  false;
				
				short FCIdefoff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  false;
				
				short Contentoff = Utils.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  false;
				
				short DFAIDoff = Utils.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  false;
				
				short APPlaboff = Utils.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  false;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return false;
			}
			Log.i(LOGTAG, "【AID】:" + Utils.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i(LOGTAG, "【select Main Application】:" + selectdffci.toString());

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
				toff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				int toffA5 = toff;

				//PDOL
				toff = Utils.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				
				toff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  false;
				
				toff = Utils.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  false;
				
				LOGSFI = tbyte[toff];
			}
			Log.i(LOGTAG, "【LOGSFI】:" + LOGSFI);
		}

		return true;
	}
	
	
	public String SelectdefaultApplet(){
		byte[] tbyte = null;
		int toff = 0;
		if(TAG7816 == null){
			Log.i(LOGTAG, "【SelectdefaultApplet.AID】: TAG7816 == null" );
			return null;
		}
		Iso7816.Response ppseres = TAG7816.selectByName(PPSE);
		if (ppseres.isOkey()) {
			tbyte = ppseres.getBytes();
			toff = 0;
			
			if((byte)0x6F == tbyte[toff++]){
				if((byte)0x81 == tbyte[toff++]){
					toff++;
				}
				short FCIoff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  null;
				
				short FCIdefoff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  null;
				
				short Contentoff = Utils.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  null;
				
				short DFAIDoff = Utils.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  null;
				
				short APPlaboff = Utils.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  null;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return null;
			}
			Log.i(LOGTAG, "【SelectdefaultApplet.AID】:" + Utils.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i(LOGTAG, "【SelectdefaultApplet.FCI】:" + selectdffci.toString());

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
				short FCIoff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(FCIoff < 0)
					return  null;
				
				short FCIdefoff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)FCIoff,(short)tbyte[FCIoff-1]);
				if(FCIoff < 0)
					return  null;
				
				short Contentoff = Utils.findValueOffByTag((short) 0x61, 
						tbyte,(short)FCIdefoff,(short)tbyte[FCIdefoff-1]);
				if(Contentoff < 0)
					return  null;
				
				short DFAIDoff = Utils.findValueOffByTag((short) 0x4F, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(DFAIDoff < 0)
					return  null;
				
				short APPlaboff = Utils.findValueOffByTag((short) 0x50, 
						tbyte,(short)Contentoff,(short)tbyte[Contentoff-1]);
				if(APPlaboff < 0)
					return  null;
				
				DFAID = new byte[tbyte[DFAIDoff - 1]];
				System.arraycopy(tbyte, DFAIDoff, DFAID, 0, DFAID.length);
				
			}else{
				return null;
			}
			Log.i(LOGTAG, "【AID】:" + Utils.toHexString(DFAID, 0, DFAID.length));
		}
		/*--------------------------------------------------------------*/
		// select Main Application
		/*--------------------------------------------------------------*/
		Iso7816.Response selectdffci = TAG7816.selectByName(DFAID);
		Log.i(LOGTAG, "【select Main Application】:" + selectdffci.toString());

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
				toff = Utils.findValueOffByTag((short) 0xA5, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				int toffA5 = toff;

				//PDOL
				toff = Utils.findValueOffByTag((short) 0x9F38, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				
				
				toff = Utils.findValueOffByTag((short) 0xBF0C, 
						tbyte,(short)toffA5,(short)tbyte[toffA5-1]);
				if(toff < 0)
					return  null;
				
				toff = Utils.findValueOffByTag((short) 0x9F4D, 
						tbyte,(short)toff,(short)tbyte[toff-1]);
				if(toff < 0)
					return  null;
				
				LOGSFI = tbyte[toff];
			}
			Log.i(LOGTAG, "【LOGSFI】:" + LOGSFI);
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

		int n = Utils.BCDtoInt(resbalance.getBytes(), 5, 4);
		strbalance = Utils.toAmountString(n / 100.0f);
		return strbalance;
	}
	
	public String getTag(String ptag) {
		String cmd = new String("80CA");
		if(4 == ptag.length()){
			cmd += ptag;
		}else if(2 == ptag.length()){
			cmd += new String("00");
			cmd += ptag;
		}else {
			Log.i(LOGTAG, "【TAG】:tag err," +ptag);
			return null;
		}
		cmd += new String("00");
		Iso7816.Response resbalance = TAG7816.sendCmd(cmd);
		if (!resbalance.isOkey()) {
			Log.i(LOGTAG, "【TAG】:Response err," +resbalance.getSw12());
			return null;
		} 
		String res = resbalance.toString();
		int reslen = res.length();
		int taglen = ptag.length();
		int lenlen = 2;
		if("81".equals(res.substring(taglen,taglen+2))){
			lenlen += 2;
		}
		return res.substring((taglen + lenlen),(reslen - 4));
	}	
}
