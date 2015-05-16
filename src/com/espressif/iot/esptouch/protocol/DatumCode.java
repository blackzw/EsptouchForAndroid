package com.espressif.iot.esptouch.protocol;

import com.espressif.iot.esptouch.task.ICodeData;
import com.espressif.iot.esptouch.util.ByteUtil;

//DatumCode是包含了sequence header + Data Code字段的数据包
public class DatumCode implements ICodeData {
	
	private final DataCode[] mDataCodes;
	private boolean isFull = false;
	private int lenUnFull = 0;
	/**
	 * Constructor of DatumCode
	 * @param apSsid the Ap's ssid
	 * @param rankSeq TODO
	 * @param apPassword the Ap's password
	 */
	//DatumCode是包含了sequence header + Data Code字段的数据包
	public DatumCode(String apSsid, byte rankSeq, String apPassword) {
		// note apPassword must before apSsid
		String info = apPassword +apSsid;
		byte[] infoBytes = ByteUtil.getBytesByString(info);
		byte[] infoBytes1 = ByteUtil.getBytesByString(apPassword);
		byte[] infoBytes2 = ByteUtil.getBytesByString(apSsid);
		int infoLen = infoBytes.length+1;
		int lenDataCode = infoLen/4;
		lenUnFull = infoLen%4;
		if(lenUnFull != 0){
			lenDataCode += 1;
			isFull = false;
		}
		else{
			isFull = true;
		}
		mDataCodes = new DataCode[lenDataCode];
		

		char[] infoChars = new char[infoLen];
		for (int i = 0; i < infoBytes1.length; i++) {
			infoChars[i] = ByteUtil.convertByte2Uint8(infoBytes1[i]);
		}
		infoChars[infoBytes1.length] = ByteUtil.convertByte2Uint8(rankSeq);
		for (int i = infoBytes1.length+1; i < (infoBytes.length+1); i++) {
			infoChars[i] = ByteUtil.convertByte2Uint8(infoBytes2[i-(infoBytes1.length+1)]);
		}
		char[] dataChars = new char[4];
		int lenDataCodesFull = infoLen/4;
		
		for (int i = 0; i < lenDataCodesFull; i++) {
			dataChars[0] = infoChars[4*i+0];
			dataChars[1] = infoChars[4*i+1];
			dataChars[2] = infoChars[4*i+2];
			dataChars[3] = infoChars[4*i+3];
			
			mDataCodes[i] = new DataCode(dataChars, i);//这里遗留一个问题，最后一个数据字段如果没有内容，不需要补全的
		}
		//最后剩下的字节如何处理
		
		if(lenUnFull != 0){
			char[] dataChars2 = new char[lenUnFull];
			for(int i = 0; i < lenUnFull; i++){
				dataChars2[i] = infoChars[lenDataCodesFull+i];
			}
			mDataCodes[lenDataCodesFull] = new DataCode(dataChars2,lenDataCodesFull, lenUnFull);
		}
		
	}
	
	@Override
	public byte[] getBytes() {
		byte[] datumCode = null;
		if(isFull == true){
			datumCode = new byte[mDataCodes.length * DataCode.DATA_CODE_LEN];
			for (int i = 0; i < mDataCodes.length; i++) {
				System.arraycopy(mDataCodes[i].getBytes(), 0, datumCode, i
						* DataCode.DATA_CODE_LEN, DataCode.DATA_CODE_LEN);
			}
		}
		else{
			datumCode = new byte[mDataCodes.length * DataCode.DATA_CODE_LEN-(4-lenUnFull)*2];
			for (int i = 0; i < (mDataCodes.length-1); i++) {
				System.arraycopy(mDataCodes[i].getBytes(), 0, datumCode, i
						* DataCode.DATA_CODE_LEN, DataCode.DATA_CODE_LEN);
			}
			System.arraycopy(mDataCodes[mDataCodes.length-1].getBytes(), 0, datumCode, (mDataCodes.length-1)
					* DataCode.DATA_CODE_LEN, lenUnFull);
		}
		
		return datumCode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		byte[] dataBytes = getBytes();
		for (int i = 0; i < dataBytes.length; i++) {
			String hexString = ByteUtil.convertByte2HexString(dataBytes[i]);
			sb.append("0x");
			if (hexString.length() == 1) {
				sb.append("0");
			}
			sb.append(hexString).append(" ");
		}
		return sb.toString();
	}
	
	@Override
	public char[] getU8s() {
		byte[] dataBytes = getBytes();
		int len = dataBytes.length / 2;
		char[] dataU8s = new char[len];
		byte high, low;
		for (int i = 0; i < len; i++) {
			high = dataBytes[i * 2];
			low = dataBytes[i * 2 + 1];
			dataU8s[i] = ByteUtil.combine2bytesToU8(high, low);
		}
		return dataU8s;
	}
}
