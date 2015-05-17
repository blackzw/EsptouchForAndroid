package com.espressif.iot.esptouch.protocol;

import com.espressif.iot.esptouch.task.ICodeData;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.CRC8;

/**
 * one data format:(data code should have 2 to 65 data)
 * 
 * 
 *              Control bit(BIT8)       data bits(BIT7~0) 
 * 1st 9bits:       1               		data
 * 2nd 9bits:       1               		data
 * 3rd 9bits:       1               		data
 * 4th 9bits:       1               		data
 * 
 * sequence header data
 * 
 * 8 7 6-0
 * 0 1 sequence crc8(low 7bits)
 * 0 1 sequence index
 * 
 * 
 * @author zhongwt@tcl.com
 * 
 */
public class DataCode implements ICodeData{
	
public static final int DATA_CODE_LEN = 12;//modified by zhongwt@tcl.com
	
	
	private static final int INDEX_MAX = 63;

	private byte mSeqHeader2[];//final
	private byte mDatas[];//final 
	private int lenU8s;//len of char[] u8s
	//private final byte mSeqHeader;
    //private final byte mDataHigh;
    //private final byte mDataLow;
    // the crc here means the crc of the data and sequence header be transformed
    // it is calculated by index and data to be transformed
    //private final byte mCrcHigh;
    //private final byte mCrcLow;
	
	/**重新写一个构造函数，直接导入byte[]
	 * */
//	public DataCode(byte[] databyte4, int index){
//		if (index > INDEX_MAX) {
//			throw new RuntimeException("index > INDEX_MAX");
//		}
//		mDatas = new byte[4];
//		mSeqHeader2 = new byte[4];
//		mSeqHeader2[0] = 0;
//		mSeqHeader2[1] = 0;//crcLow7
//		mSeqHeader2[2] = 0;
//		mSeqHeader2[3] = ByteUtil.convertUint8toByte((char)((index&0xFF)|0x80));
//		byte[] buf = new byte[5];
//		buf[0] = ByteUtil.convertUint8toByte((char)(index&0xFF));
//		System.arraycopy(databyte4, 0, buf, 1, 4);//将databyte4的内容复制到buf里面
//		CRC8 crc8 = new CRC8();
//		crc8.update(buf);
//		byte crcLow7 = ByteUtil.convertUint8toByte((char)(crc8.getValue()&0x7F|0x80));
//		mSeqHeader2[1] = crcLow7;
//		
//	}
    /**
     * Constructor of DataCode
     * @param u8s the character to be transformed
     * @param index the index of the char
     */
	public DataCode(char[] u8s, int index) {
		if (index > INDEX_MAX) {
			throw new RuntimeException("index > INDEX_MAX");
		}
		lenU8s = 4;
		mDatas = new byte[4];
		mSeqHeader2 = new byte[4];
		mSeqHeader2[0] = 0;
		mSeqHeader2[1] = 0;//crcLow7
		mSeqHeader2[2] = 0;
		mSeqHeader2[3] = ByteUtil.convertUint8toByte((char)((index&0xFF)|0x80));

		byte[] buf = new byte[5];
		buf[0] = ByteUtil.convertUint8toByte((char)(index&0xFF));//mSeqHeader2[3];
		for(int i = 1; i < 5; i++){
			buf[i] = ByteUtil.convertUint8toByte(u8s[i-1]);
			//mDatas[i-1] = ByteUtil.convertUint8toByte((char)(u8s[i-1]| 0x80)) ;
			mDatas[i-1] = ByteUtil.convertUint8toByte(u8s[i-1]);
		}
		CRC8 crc8 = new CRC8();
		crc8.update(buf);

		//byte crcLow7 = ByteUtil.convertUint8toByte((char)(crc8.getValue()&0x7F|0x80));
		byte crcLow7 = ByteUtil.convertUint8toByte((char)(crc8.getValue()&0x7F|0x80));

		
		mSeqHeader2[1] = crcLow7;

	}
	/**
     * Constructor of DataCode
     * @param u8s the character to be transformed
     * @param index the index of the char
     * @param len_less_4 the length of u8s < 4
     */
	public DataCode(char[] u8s, int index, int lenLess4){
		if (index > INDEX_MAX) {
			throw new RuntimeException("index > INDEX_MAX");
		}
		if(lenLess4 > 4){
			throw new RuntimeException("len_less_4 > 4");
		}
		lenU8s = lenLess4;
		mDatas = new byte[lenLess4];
		mSeqHeader2 = new byte[4];
		mSeqHeader2[0] = 0;
		mSeqHeader2[1] = 0;//crcLow7
		mSeqHeader2[2] = 0;
		mSeqHeader2[3] = ByteUtil.convertUint8toByte((char)(index|0x80));
		byte[] buf = new byte[lenLess4+1];
		buf[0] = ByteUtil.convertUint8toByte((char)(index&0xFF));//mSeqHeader2[3];
		for(int i = 1; i < lenLess4+1; i++){
			buf[i] = ByteUtil.convertUint8toByte(u8s[i-1]);
			//mDatas[i-1] = ByteUtil.convertUint8toByte((char)(u8s[i-1]| 0x80)) ;
			mDatas[i-1] = ByteUtil.convertUint8toByte(u8s[i-1]);
		}
		CRC8 crc8 = new CRC8();
		crc8.update(buf);
		byte crcLow7 = ByteUtil.convertUint8toByte((char)(crc8.getValue()&0x7F|0x80));
		mSeqHeader2[1] = crcLow7;
	}
	
	@Override
	public byte[] getBytes() {
		byte[] dataBytes = new byte[4 + lenU8s*2];
		dataBytes[0] = mSeqHeader2[0];
		dataBytes[1] = mSeqHeader2[1];
		dataBytes[2] = mSeqHeader2[2];
		dataBytes[3] = mSeqHeader2[3];
		
		for(int i = 0; i < lenU8s; i++){
			dataBytes[i*2+4] = 0x01;
			dataBytes[i*2+5] = mDatas[i];
		}

//		dataBytes[0] = 0x00;
//		dataBytes[1] = mDatas[0];
//		dataBytes[2] = 0x00;
//		dataBytes[3] = mDatas[1];
//		dataBytes[4] = 0x00;
//		dataBytes[5] = mDatas[2];
//		dataBytes[6] = 0x00;
//		dataBytes[7] = mDatas[3];
		return dataBytes;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		byte[] dataBytes = getBytes();
		for (int i = 0; i < DATA_CODE_LEN; i++) {
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
		throw new RuntimeException("DataCode don't support getU8s()");
	}

}
