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
public class DataCode2 implements ICodeData{
	
	public static final int DATA_CODE_LEN = 6;
	
	private static final int INDEX_MAX = 63;

	private byte mSeqHeader;//final 
    private byte mDataHigh;//final 
    private byte mDataLow;//final 
    // the crc here means the crc of the data and sequence header be transformed
    // it is calculated by index and data to be transformed
    private byte mCrcHigh;//final 
    private byte mCrcLow;//final 
	
    /**
     * Constructor of DataCode
     * @param u8 the character to be transformed
     * @param index the index of the char
     */
	public void DataCode(char u8, int index) {
		if (index > INDEX_MAX) {
			throw new RuntimeException("index > INDEX_MAX");
		}
		byte[] dataBytes = ByteUtil.splitUint8To2bytes(u8);
		mDataHigh = dataBytes[0];
		mDataLow = dataBytes[1];
		CRC8 crc8 = new CRC8();
		crc8.update(ByteUtil.convertUint8toByte(u8));
		crc8.update(index);
		byte[] crcBytes = ByteUtil.splitUint8To2bytes((char) crc8.getValue());
		mCrcHigh = crcBytes[0];
		mCrcLow = crcBytes[1];
		if (mDataLow == 0 && mCrcLow == 0) {
			mSeqHeader = (byte) (index | 101 << 6);
		} else {
			mSeqHeader = (byte) (index | 100 << 6);
		}
	}
	
	@Override
	public byte[] getBytes() {
		byte[] dataBytes = new byte[DATA_CODE_LEN];
		dataBytes[0] = 0x00;
		dataBytes[1] = ByteUtil.combine2bytesToOne(mCrcHigh,mDataHigh);
		dataBytes[2] = 0x01;
		dataBytes[3] = mSeqHeader;
		dataBytes[4] = 0x00;
		dataBytes[5] = ByteUtil.combine2bytesToOne(mCrcLow, mDataLow);
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
