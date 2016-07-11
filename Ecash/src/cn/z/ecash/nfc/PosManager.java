package cn.z.ecash.nfc;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import cn.z.ecash.commn.ByteUtil;

public class PosManager {
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
	private PosManager(){
		
	}
	
	public static String getCAModuls(String prid,String pcaindex){
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
	
	public static String getCAExp(String prid,String pcaindex){
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