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

package cn.z.ecash.nfc.pboc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.nfc.tech.IsoDep;
import android.util.Log;

import cn.z.ecash.commn.*;
import cn.z.ecash.nfc.CardManager;
import cn.z.ecash.nfc.Iso7816;

@SuppressLint("NewApi") public class PbocCard {
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

	public static Map<String, Object> read(IsoDep tech, Resources res) {
		Log.i("PBOC", "【start】:");
		final Iso7816.Tag tag = new Iso7816.Tag(tech);
		Log.i("PBOC", "【get 7816TAG】:");

		tag.connect();
		Log.i("PBOC", "connect:");

		PbocCard card = null;

		do {
//			if ((card = PBOCTEST.load(tag, res)) != null)
//				break;
			
			if ((card = EcashCard.read(tag, res)) != null){
				Log.i("PBOC", "【Ecash Card Load】:");
				break;
			}
			
//			if ((card = PBOCGPOdebug.load(tag, res)) != null)
//				break;
			
//			if ((card = EcashCardGPO.load(tag, res)) != null)
//				break;
			
//			if ((card = ShenzhenTong.load(tag, res)) != null)
//				break;
//
//			if ((card = BeijingMunicipal.load(tag, res)) != null)
//				break;
//
//			if ((card = ChanganTong.load(tag, res)) != null)
//				break;
//
//			if ((card = WuhanTong.load(tag, res)) != null)
//				break;
//
//			if ((card = YangchengTong.load(tag, res)) != null)
//				break;
//			
//			if ((card = HardReader.load(tag, res)) != null)
//				break;
		} while (false);

		tag.close();

		Map<String,Object>  tmap = new HashMap();
		tmap.put("name", card.name);
		tmap.put("id", card.id);
		tmap.put("serl", card.serl);
		tmap.put("version", card.version);
		tmap.put("count", card.count);
		tmap.put("cash", card.cash);
		tmap.put("log", card.log);
		return tmap;
	}

	protected PbocCard(Iso7816.Tag tag) {
		id = tag.getID().toString();
	}

	protected void parseInfo(Iso7816.Response data, int dec, boolean bigEndian) {
		if (!data.isOkey() || data.size() < 30) {
			serl = version = date = count = null;
			return;
		}

		final byte[] d = data.getBytes();
		if (dec < 1 || dec > 10) {
			serl = Utils.toHexString(d, 10, 10);
		} else {
			final int sn = bigEndian ? Utils.toIntR(d, 19, dec) : Utils.toInt(d,
					20 - dec, dec);

			serl = String.format("%d", 0xFFFFFFFFL & sn);
		}

		version = (d[9] != 0) ? String.valueOf(d[9]) : null;
		date = String.format("%02X%02X.%02X.%02X - %02X%02X.%02X.%02X", d[20],
				d[21], d[22], d[23], d[24], d[25], d[26], d[27]);
		count = null;
	}
	protected static boolean addLog(final Iso7816.Response r,
			ArrayList<byte[]> l) {
		if (!r.isOkey())
			return false;

		final byte[] raw = r.getBytes();
		final int N = raw.length - 23;
		if (N < 0)
			return false;

		for (int s = 0, e = 0; s <= N; s = e) {
			l.add(Arrays.copyOfRange(raw, s, (e = s + 23)));
		}

		return true;
	}

	protected static ArrayList<byte[]> readLog(Iso7816.Tag tag, int sfi) {
		final ArrayList<byte[]> ret = new ArrayList<byte[]>(MAX_LOG);
		final Iso7816.Response rsp = tag.readRecord(sfi);
		if (rsp.isOkey()) {
			addLog(rsp, ret);
		} else {
			for (int i = 1; i <= MAX_LOG; ++i) {
				if (!addLog(tag.readRecord(sfi, i), ret))
					break;
			}
		}

		return ret;
	}

	protected void parseLog(ArrayList<byte[]>... logs) {
		final StringBuilder r = new StringBuilder();

		for (final ArrayList<byte[]> log : logs) {
			if (log == null)
				continue;

			if (r.length() > 0)
				r.append("<br />--------------");

			for (final byte[] v : log) {
				final int cash = Utils.toInt(v, 5, 4);
				if (cash > 0) {
					r.append("<br />").append(
							String.format("%02X%02X.%02X.%02X %02X:%02X ",
									v[16], v[17], v[18], v[19], v[20], v[21],
									v[22]));

					final char t = (v[9] == TRANS_CSU || v[9] == TRANS_CSU_CPX) ? '-'
							: '+';

					r.append(t).append(Utils.toAmountString(cash / 100.0f));

					final int over = Utils.toInt(v, 2, 3);
					if (over > 0)
						r.append(" [o:")
								.append(Utils.toAmountString(over / 100.0f))
								.append(']');

					r.append(" [").append(Utils.toHexString(v, 10, 6))
							.append(']');
				}
			}
		}

		this.log = r.toString();
	}

	protected void parseBalance(Iso7816.Response data) {
		if (!data.isOkey() || data.size() < 4) {
			cash = null;
			return;
		}

		int n = Utils.toInt(data.getBytes(), 0, 4);
		if (n > 100000 || n < -100000)
			n -= 0x80000000;

		cash = Utils.toAmountString(n / 100.0f);
	}
	
	protected void parseData(String ptag,Iso7816.Response data) {
		if (!data.isOkey() || data.size() < 1) {
			return;
		} 

		byte[] d = data.getBytes();
		if(("ATC".equals(ptag))){
			//ATC,tag = 9F36,len = 02.
			int cnt = Utils.toInt(d, 3, 2);
			count = Utils.toIntegerString(cnt);
		}
	}
}
