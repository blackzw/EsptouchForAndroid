package com.espressif.iot.esptouch.protocol;

import android.util.Log;

import com.espressif.iot.esptouch.task.ICodeData;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.CRC8;

import java.io.UnsupportedEncodingException;
import java.util.Random;

//DatumCode�ǰ�����sequence header + Data Code�ֶε����ݰ�
public class DatumCode implements ICodeData {
	
	private final DataCode[] mDataCodes;
	private boolean isFull = false;
	private int lenUnFull = 0;
	private byte token = 0;//�����������Ӧ�ò��Э�飬�豸�㲥�󷵻ظ�tokenȷ�����óɹ�
	private byte[] mOriginData;//pwd+token+ssid
	private byte[] payload;//��������
	private byte randomByte() {//����˭����
        byte randByte = 0x0;
        Random r = new Random();
        randByte = (byte)r.nextInt(0x100);
        return randByte;
    }
	
	//�µĹ��캯��
//	public DatumCode(String apSsid, String apPassword, boolean isNew){
//		int oriDataLen = (apSsid.length() + apPassword.length()) + 1;
//		int numDataCode = (int)Math.ceil((double)(oriDataLen/4.0));
//		mOriginData = buildOriginalData(apSsid, apPassword);
//		mDataCodes = new DataCode[numDataCode];
//		payload = buildValidPayload(mOriginData);
//	}
//	private static int USE_BITS;
//	static {
//        USE_BITS = 0x9;        
//    }
//	private byte[] buildOriginalData(String ssid, String pwd) {
//        byte[] oriData = null;
//        byte rand = randomByte();
//        int oriDataLen = (ssid.length() + pwd.length()) + 1;
//        oriData = new byte[oriDataLen];
//        try {
//            System.arraycopy(pwd.getBytes("UTF8"), 0x0, oriData, 0x0, pwd.length());
//            oriData[pwd.length()] = rand;
//            System.arraycopy(ssid.getBytes("UTF8"), 0x0, oriData, (pwd.length() + 0x1), ssid.length());
//            return oriData;
//        } catch(UnsupportedEncodingException e) {
//            Log.e("AirKissDemo.AirKissProtocol", "dataStr.getBytes error");
//            e.printStackTrace();
//        }
//        return oriData;
//    }
//	private byte[] buildValidPayload(byte[] oriData) {
//        byte[] reData = null;
//        int desDataLen = (int)Math.ceil(((double)(oriData.length << 0x3) / ((double)USE_BITS - 1.0)));
//        reData = new byte[desDataLen];
//        for(int i = 0; i < desDataLen; i++){
//        	int idx = ((USE_BITS - 0x1) * i) >> 0x3;
//        int left = ((oriData[idx] & 0xff) & 0xff) >> (USE_BITS + 0x1);
//        int right = 0;
//        	if((i+1)!=desDataLen){
//        		right = (oriData[(idx + 0x1)] & 0xff) >> (USE_BITS + 0x1);
//        	}
//	        reData[i] = (byte)(left | right);
//        }
//        
//        return reData;
//    }
//	private byte[] getSequencesData(byte[] validPayload) {
//        byte[] sequencesData = null;
//        byte[] seqData = null;
//        int totalSeqNum = (int)Math.ceil((((double)validPayload.length * 1.0) / 4.0));
//        sequencesData = new byte[(totalSeqNum*2 + validPayload.length)];
//        int offset = 0x0;
//        int curSeqLen = 0x0;
//        for(int i = 0; i < totalSeqNum; i++){
//        	if(i == (totalSeqNum - 0x1)) {
//                curSeqLen = validPayload.length - (i * 0x4);
//            } else {
//                curSeqLen = 0x4;
//            }
//        	offset = i * 0x4;
//        	seqData = getSequenceField((byte)i, validPayload, offset, curSeqLen);
//            System.arraycopy(seqData, 0x0, sequencesData, ((i << 0x1) + offset), seqData.length);
//        }     
//        
//        return sequencesData;
//    }
//	
//	private byte[] getSequenceField(byte seqIdx, byte[] validPayload, int offset, int len) {
//        byte[] tmp = new byte[(len + 0x1)];
//        byte[] seq = new byte[(len + 0x2)];
//        tmp[0x0] = seqIdx;
//        System.arraycopy(validPayload, offset, tmp, 0x1, len);
//        CRC8 crc8 = new CRC8();
//        crc8.update(tmp);
//        seq[0] = ByteUtil.convertUint8toByte((char)((crc8.getValue()&0x7F)|0x80));
//        seq[1] = ByteUtil.convertUint8toByte((char)((seqIdx&0x7F)|0x80));
//        
//        System.arraycopy(validPayload, offset, seq, 2, len);        
//        return seq;
//    }
	/**
	 * Constructor of DatumCode
	 * @param apSsid the Ap's ssid
	 * @param apPassword the Ap's password
	 * @param rankSeq TODO
	 */
	//DatumCode�ǰ�����sequence header + Data Code�ֶε����ݰ�
	public DatumCode(String apSsid, String apPassword) {
		token = randomByte();//��ȡ�������
		// note apPassword must before apSsid
		String info = apPassword +apSsid;
		byte[] infoBytes = ByteUtil.getBytesByString(info);//ֻ�������ssid������
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
		byte[] infoBytes0 = new byte[infoLen];
//		for (int i = 0; i < infoBytes1.length; i++) {
//			infoChars[i] = ByteUtil.convertByte2Uint8(infoBytes1[i]);
//		}
//		infoChars[infoBytes1.length] = ByteUtil.convertByte2Uint8(token);
//		for (int i = infoBytes1.length+1; i < (infoBytes.length+1); i++) {
//			infoChars[i] = ByteUtil.convertByte2Uint8(infoBytes2[i-(infoBytes1.length+1)]);
//		}
		System.arraycopy(infoBytes1, 0, infoBytes0, 0,  infoBytes1.length);
		infoBytes0[infoBytes1.length] = token;
		System.arraycopy(infoBytes2, 0, infoBytes0, (infoBytes1.length+1),  infoBytes2.length);
		
		char[] dataChars = new char[4];
		int lenDataCodesFull = infoLen/4;
		
		for (int i = 0; i < lenDataCodesFull; i++) {
			dataChars[0] = ByteUtil.convertByte2Uint8(infoBytes0[4*i+0]);
			dataChars[1] = ByteUtil.convertByte2Uint8(infoBytes0[4*i+1]);
			dataChars[2] = ByteUtil.convertByte2Uint8(infoBytes0[4*i+2]);
			dataChars[3] = ByteUtil.convertByte2Uint8(infoBytes0[4*i+3]);
			
			mDataCodes[i] = new DataCode(dataChars, i);//��������һ�����⣬���һ�������ֶ����û�����ݣ�����Ҫ��ȫ��
		}
		//���ʣ�µ��ֽ���δ���
		
		if(lenUnFull != 0){
			char[] dataChars2 = new char[lenUnFull];
			for(int i = 0; i < lenUnFull; i++){
				dataChars2[i] = ByteUtil.convertByte2Uint8(infoBytes0[4*lenDataCodesFull+i]);//infoChars[lenDataCodesFull+i];
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
					* DataCode.DATA_CODE_LEN, (lenUnFull*+4));
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
