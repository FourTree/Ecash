package cn.z.ecash.commn;

import java.util.ArrayList;

/**
 * TAG信息描述 用于调试和TAG检测
 * 
 * @author wangyun
 * 
 */
public class TAGInformation {

	private static class TagDisp {
		/**
		 * TAG描述信息
		 */
		public String disp = "";

		/**
		 * TAG所属TAG,用于检查，存在多个时，在base_tag_ext中描述
		 */
		public int base_tag = 0;

		/**
		 * TAG所属TAG,用于检查，存在多个时，在base_tag_ext中描述
		 */
		public int base_tag_ext = 0;

		/**
		 * 对应描述TAG信息
		 */
		public int tag = 0;

		/**
		 * 构造TagDisp
		 * 
		 * @param disp
		 * @param base_tag
		 * @param tag
		 * @param base_tag_ext
		 */
		public TagDisp(String disp, int base_tag, int tag, int base_tag_ext) {
			this.disp = disp;
			this.base_tag = base_tag;
			this.tag = tag;
			this.base_tag_ext = base_tag_ext;
		}

	}

	private static ArrayList<TagDisp> tagsDisp = new ArrayList<TagDisp>();

	private static void newDispItem(String info, int base_tag,
			int base_tag_ext, int tag) {
		tagsDisp.add(new TagDisp(info, base_tag, tag, base_tag_ext));
	}

	/**
	 * 构造tagsDisp列表，受次调用
	 */
	private static void initTagsDisp() {
		// emv tag information
		// EMV v4[1].2 Book 3 Application Specification CR05_20090124020803.pdf
		// Annex B Data Elements Table
		// Data Elements by Tag 描述的内容
		newDispItem("Issuer Identification Number (IIN)", 0xBF0C, 0x73, 0x42);
		newDispItem("Application Identifier (AID) - card", 0x61, 0, 0x4F);
		newDispItem("Application Label", 0x61, 0xA5, 0x50);
		newDispItem("Track 2 Equivalent Data", 0x70, 0x77, 0x57);
		newDispItem("Application Primary Account Number (PAN)", 0x70, 0x77,
				0x5A);
		newDispItem("Cardholder Name", 0x70, 0x77, 0x5F20);
		newDispItem("Application Expiration Date", 0x70, 0x77, 0x5F24);
		newDispItem("Application Effective Date", 0x70, 0x77, 0x5F25);
		newDispItem("Issuer Country Code", 0x70, 0x77, 0x5F28);
		newDispItem("Transaction Currency Code", 0, 0, 0x5F2A);
		newDispItem("Language Preference", 0xA5, 0, 0x5F2D);
		newDispItem("Service Code", 0x70, 0x77, 0x5F30);
		newDispItem("Application Primary Account Number (PAN) Sequence Number",
				0x70, 0x77, 0x5F34);
		newDispItem("Transaction Currency Exponent", 0, 0, 0x5F36);
		newDispItem("Issuer URL", 0xBF0C, 0x73, 0x5F50);
		newDispItem("International Bank Account Number (IBAN)", 0xBF0C, 0x73,
				0x5F53);
		newDispItem("Bank Identifier Code (BIC)", 0xBF0C, 0x73, 0x5F54);
		newDispItem("Issuer Country Code (alpha2 format)", 0xBF0C, 0x73, 0x5F55);
		newDispItem("Issuer Country Code (alpha3 format)", 0xBF0C, 0x73, 0x5F56);
		newDispItem("Application Template", 0x70, 0x77, 0x61);
		newDispItem("File Control Information (FCI) Template", 0, 0, 0x6F);
		newDispItem("READ RECORD Response Message Template", 0, 0, 0x70);
		newDispItem("Issuer Script Template 1", 0, 0, 0x71);
		newDispItem("Issuer Script Template 2", 0, 0, 0x72);
		newDispItem("Directory Discretionary Template", 0x61, 0, 0x73);
		newDispItem("Response Message Template Format 2", 0, 0, 0x77);
		newDispItem("Response Message Template Format 1", 0, 0, 0x80);
		newDispItem("Amount, Authorised (Binary)", 0, 0, 0x81);
		newDispItem("Application Interchange Profile", 0x77, 0x80, 0x82);
		newDispItem("Command Template", 0, 0, 0x83);
		newDispItem("Dedicated File (DF) Name", 0x6F, 0, 0x84);
		newDispItem("Issuer Script Command", 0x71, 0x72, 0x86);
		newDispItem("Application Priority Indicator", 0x61, 0xA5, 0x87);
		newDispItem("Short File Identifier (SFI)", 0xA5, 0, 0x88);
		newDispItem("Authorisation Code", 0, 0, 0x89);
		newDispItem("Authorisation Response Code", 0, 0, 0x8A);
		newDispItem("Card Risk Management Data Object List 1 (CDOL1)", 0x70,
				0x77, 0x8C);
		newDispItem("Card Risk Management Data Object List 2 (CDOL2)", 0x70,
				0x77, 0x8D);
		newDispItem("Cardholder Verification Method (CVM) List", 0x70, 0x77,
				0x8E);
		newDispItem("Certification Authority Public Key Index", 0x70, 0x77,
				0x8F);
		newDispItem("Issuer Public Key Certificate", 0x70, 0x77, 0x90);
		newDispItem("Issuer Authentication Data", 0, 0, 0x91);
		newDispItem("Issuer Public Key Remainder", 0x70, 0x77, 0x92);
		newDispItem("Signed Static Application Data", 0x70, 0x77, 0x93);
		newDispItem("Application File Locator (AFL)", 0x77, 0x80, 0x94);
		newDispItem("Terminal Verification Results", 0, 0, 0x95);
		newDispItem("Transaction Certificate Data Object List (TDOL)", 0x70,
				0x77, 0x97);
		newDispItem("Transaction Certificate (TC) Hash Value", 0, 0, 0x98);
		newDispItem("Transaction Personal Identification Number (PIN) Data", 0,
				0, 0x99);
		newDispItem("Transaction Date", 0, 0, 0x9A);
		newDispItem("Transaction Status Information", 0, 0, 0x9B);
		newDispItem("Transaction Type", 0, 0, 0x9C);
		newDispItem("Directory Definition File (DDF) Name", 0x61, 0, 0x9D);
		newDispItem("Acquirer Identifier", 0, 0, 0x9F01);
		newDispItem("Amount, Authorised (Numeric)", 0, 0, 0x9F02);
		newDispItem("Amount, Other (Numeric)", 0, 0, 0x9F03);
		newDispItem("Amount, Other (Binary)", 0, 0, 0x9F04);
		newDispItem("Application Discretionary Data", 0x70, 0x77, 0x9F05);
		newDispItem("Application Identifier (AID) - terminal", 0, 0, 0x9F06);
		newDispItem("Application Usage Control", 0x70, 0x77, 0x9F07);
		newDispItem("Application Version Number", 0x70, 0x77, 0x9F08);
		newDispItem("Application Version Number", 0, 0, 0x9F09);
		newDispItem("Cardholder Name Extended", 0x70, 0x77, 0x9F0B);
		newDispItem("Issuer Action Code - Default", 0x70, 0x77, 0x9F0D);
		newDispItem("Issuer Action Code - Denial", 0x70, 0x77, 0x9F0E);
		newDispItem("Issuer Action Code - Online", 0x70, 0x77, 0x9F0F);
		newDispItem("Issuer Application Data", 0x77, 0x80, 0x9F10);
		newDispItem("Issuer Code Table Index", 0xA5, 0, 0x9F11);
		newDispItem("Application Preferred Name", 0x61, 0xA5, 0x9F12);
		newDispItem(
				"Last Online Application Transaction Counter (ATC) Register",
				0, 0, 0x9F13);
		newDispItem("Lower Consecutive Offline Limit", 0x70, 0x77, 0x9F14);
		newDispItem("Merchant Category Code", 0, 0, 0x9F15);
		newDispItem("Merchant Identifier", 0, 0, 0x9F16);
		newDispItem("Personal Identification Number (PIN) Try Counter", 0, 0,
				0x9F17);
		newDispItem("Issuer Script Identifier", 0x71, 0x72, 0x9F18);
		newDispItem("Terminal Country Code", 0, 0, 0x9F1A);
		newDispItem("Terminal Floor Limit", 0, 0, 0x9F1B);
		newDispItem("Terminal Identification", 0, 0, 0x9F1C);
		newDispItem("Terminal Risk Management Data", 0, 0, 0x9F1D);
		newDispItem("Interface Device (IFD) Serial Number", 0, 0, 0x9F1E);
		newDispItem("Track 1 Discretionary Data", 0x70, 0x77, 0x9F1F);
		newDispItem("Track 2 Discretionary Data", 0x70, 0x77, 0x9F20);
		newDispItem("Transaction Time", 0, 0, 0x9F21);
		newDispItem("Certification Authority Public Key Index", 0, 0, 0x9F22);
		newDispItem("Upper Consecutive Offline Limit", 0x70, 0x77, 0x9F23);
		newDispItem("Application Cryptogram", 0x77, 0x80, 0x9F26);
		newDispItem("Cryptogram Information Data", 0x77, 0x80, 0x9F27);
		newDispItem("ICC PIN Encipherment Public Key Certificate", 0x70, 0x77,
				0x9F2D);
		newDispItem("ICC PIN Encipherment Public Key Exponent", 0x70, 0x77,
				0x9F2E);
		newDispItem("ICC PIN Encipherment Public Key Remainder", 0x70, 0x77,
				0x9F2F);
		newDispItem("Issuer Public Key Exponent", 0x70, 0x77, 0x9F32);
		newDispItem("Terminal Capabilities", 0, 0, 0x9F33);
		newDispItem("Cardholder Verification Method (CVM) Results", 0, 0,
				0x9F34);
		newDispItem("Terminal Type", 0, 0, 0x9F35);
		newDispItem("Application Transaction Counter (ATC)", 0x77, 0x80, 0x9F36);
		newDispItem("Unpredictable Number", 0, 0, 0x9F37);
		newDispItem("Processing Options Data Object List (PDOL)", 0xA5, 0,
				0x9F38);
		newDispItem("Point-of-Service (POS) Entry Mode", 0, 0, 0x9F39);
		newDispItem("Amount, Reference Currency", 0, 0, 0x9F3A);
		newDispItem("Application Reference Currency", 0x70, 0x77, 0x9F3B);
		newDispItem("Transaction Reference Currency Code", 0, 0, 0x9F3C);
		newDispItem("Transaction Reference Currency Exponent", 0, 0, 0x9F3D);
		newDispItem("Additional Terminal Capabilities", 0, 0, 0x9F40);
		newDispItem("Transaction Sequence Counter", 0, 0, 0x9F41);
		newDispItem("Application Currency Code", 0x70, 0x77, 0x9F42);
		newDispItem("Application Reference Currency Exponent", 0x70, 0x77,
				0x9F43);
		newDispItem("Application Currency Exponent", 0x70, 0x77, 0x9F44);
		newDispItem("Data Authentication Code", 0, 0, 0x9F45);
		newDispItem("ICC Public Key Certificate", 0x70, 0x77, 0x9F46);
		newDispItem("ICC Public Key Exponent", 0x70, 0x77, 0x9F47);
		newDispItem("ICC Public Key Remainder", 0x70, 0x77, 0x9F48);
		newDispItem("Dynamic Data Authentication Data Object List (DDOL)",
				0x70, 0x77, 0x9F49);
		newDispItem("Static Data Authentication Tag List", 0x70, 0x77, 0x9F4A);
		newDispItem("Signed Dynamic Application Data", 0, 0, 0x9F4B);
		newDispItem("ICC Dynamic Number", 0, 0, 0x9F4C);
		newDispItem("Log Entry", 0xBF0C, 0x73, 0x9F4D);
		newDispItem("Merchant Name and Location", 0, 0, 0x9F4E);
		newDispItem("Log Format", 0, 0, 0x9F4F);
		newDispItem("File Control Information (FCI) Proprietary Template",
				0x6F, 0, 0xA5);
		newDispItem("File Control Information (FCI) Issuer Discretionary Data",
				0xA5, 0, 0xBF0C);

		// Terminal Data Elements
		newDispItem("Application Identifier (AID) - terminal", 0, 0, 0x9F06);
	}

	/**
	 * 获取Tag描述信息
	 * 
	 * @param tag
	 * @return
	 */
	public static String getTagInformation(int tag) {
		if (tagsDisp.size() == 0) {
			initTagsDisp();
		}

		for (int i = 0; i < tagsDisp.size(); i++) {
			if (tagsDisp.get(i).tag == tag) {
				return tagsDisp.get(i).disp;
			}
		}

		return "know tag information";
	}
}