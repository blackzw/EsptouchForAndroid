package com.espressif.iot.esptouch.protocol;

import com.espressif.iot.esptouch.task.IEsptouchGenerator;
import com.espressif.iot.esptouch.util.ByteUtil;

//这个是最主要的产生SmartConfig广播报文的函数
public class EsptouchGenerator implements IEsptouchGenerator {

	private final byte[][] mGcBytes2;//Guide Code
	private final byte[][] mMcBytes2;//Magic Code
	private final byte[][] mPcBytes2;//Prefix Code
	private final byte[][] mDcBytes2;//Data Code

	/**
	 * Constructor of EsptouchGenerator, it will cost some time(maybe a bit much)
	 * 
	 * @param apSsid
	 *            the Ap's ssid
	 * @param apPassword
	 *            the Ap's password
	 */
	public EsptouchGenerator(String apSsid, String apPassword) {

		byte[] apSsidBytes = ByteUtil.getBytesByString(apSsid);
		byte[] apPasswordBytes = ByteUtil.getBytesByString(apPassword);
		
		// the u8 total len of apSsid and apPassword
		//现在长度是对了，能够显示SSID，但是Password还是不正确，确实需要增加1
		char totalLen = (char) (apSsidBytes.length + apPasswordBytes.length+1);
		// the u8 len of apPassword
		char pwdLen = (char) apPasswordBytes.length;
		char ssidLen = (char)apSsidBytes.length;

		// generate guide code
		//引导码没问题
		GuideCode gc = new GuideCode();
		byte[] gcBytes1 = gc.getBytes();
		mGcBytes2 = new byte[gcBytes1.length][];

		for (int i = 0; i < mGcBytes2.length; i++) {
			mGcBytes2[i] = ByteUtil.genSpecBytes(gcBytes1[i]);
		}

		// generate magic code
		//magic code已经OK了
		MagicCode mc = new MagicCode(totalLen, apSsid);		
		char[] mcU81 = mc.getU8s();
		mMcBytes2 = new byte[mcU81.length][];

		for (int i = 0; i < mMcBytes2.length; i++) {
			mMcBytes2[i] = ByteUtil.genSpecBytes(mcU81[i]);
		}

		// generate prefix code
		//理论上，这个应该也OK了，
		PrefixCode pc = new PrefixCode(pwdLen);
		char[] pcU81 = pc.getU8s();
		mPcBytes2 = new byte[pcU81.length][];

		for (int i = 0; i < mPcBytes2.length; i++) {
			mPcBytes2[i] = ByteUtil.genSpecBytes(pcU81[i]);
		}

		// generate data code
		//除了SSID和PWD外，中间还应该增加一个BYTE的SEQ序列号
		//byte rankseq = ByteUtil.convertUint8toByte((char)(0x11));
		DatumCode dc = new DatumCode(apSsid, apPassword);
		char[] dcU81 = dc.getU8s();
		mDcBytes2 = new byte[dcU81.length][];

		for (int i = 0; i < mDcBytes2.length; i++) {
			mDcBytes2[i] = ByteUtil.genSpecBytes(dcU81[i]);
		}
	}

	@Override
	public byte[][] getGCBytes2() {

		return mGcBytes2;
	}

	@Override
	public byte[][] getMCBytes2() {
		return mMcBytes2;
	}

	@Override
	public byte[][] getPCBytes2() {
		return mPcBytes2;
	}

	@Override
	public byte[][] getDCBytes2() {
		return mDcBytes2;
	}

}
