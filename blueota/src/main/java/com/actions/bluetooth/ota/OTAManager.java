package com.actions.bluetooth.ota;

import com.actions.ibluz.factory.IBluzDevice;
import com.tencent.mars.xlog.Log;

import android.content.Context;
import android.content.SharedPreferences;

import com.actions.ibluz.factory.IBluzIO;
import com.actions.ibluz.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

/**
 * Created by InidHu on 2018/11/27.
 */

public class OTAManager {
    private final static String TAG = "OTAManager";
    private Context mContext;

    public final static int STATE_UNKNOWN = 0;
    public final static int STATE_IDLE = 1;
    public final static int STATE_PREPARING = 2;
    public final static int STATE_PREPARED = 3;
    public final static int STATE_TRANSFERRING = 4;
    public final static int STATE_TRANSFERRED = 5;

    private final static int FRAME_SIZE = 256;

    private static final int HANDSHAKE_TIMEOUT = 20000;

    private boolean mRunning = false;
    private ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    private RunnableScheduledFuture<Void> mSFRemoteTimeout;
    private int timeout = 10;

 //   private FileInputStream mOTAInputStream;
    private RandomAccessFile mOTARandomAccessFile;

    private IBluzIO mIO;
    private int mState = STATE_IDLE;

    private long mOTAFileLength;
    private String mFwVersion = "1.01";
    private int mOTAMode = 0;
    private int mBatteryThreshold = 30;
    private byte mVRAM[] = new byte[16];
    private int mWaitTimeout;
    private int mRestartTimeout;
    private int mOtaUnit;
    private int mInterval;
    private int mAckEnable;
    private int mWriteBytes;

    private boolean isStressTest = false;
    private float mStressVersion = 1.01f;

    private ProcessRunnable mProcessRunnable = new ProcessRunnable();
    private Thread mPollThread;

    private OTAListener mListener;

    Boolean remoteCrcSupport = false;

    public interface OTAListener {
        void onStatus(int state);
        void onAudioDataReceived(int psn, int len, byte[] data);
        void onRemoteStatusReceived(RemoteStatus status);
        void onProgress(int progress, int total);
        void onError(int errcode, String errmsg);
        void onWriteBytes(int count);
    }


    public OTAManager(Context context, IBluzIO io) {
        mIO = io;
        mContext = context;
        mState = STATE_IDLE;
        mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        mRunning = false;
    }

    public void setListener(IBluzDevice.OnConnectionListener listener) {
        mListener = listener;
    }

    public boolean setOTAFile(String path) {
        if (path == null || path.length() <= 0)
            return false;

        File file = new File(path);

        if (!file.exists())
            return false;

        mOTAFileLength = file.length();
        Log.v(TAG, "setOTAFile length: " + mOTAFileLength);

        try {
        //    mOTAInputStream = new FileInputStream(file);
            mOTARandomAccessFile = new RandomAccessFile(path, "r");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void setStressTest(boolean stress) {
        isStressTest = stress;
        mStressVersion = 0.01f;
    }

    private boolean readOTAHeader() {
        if (mOTARandomAccessFile == null) {
            Log.e("main_readOTAHeader", "null");
            return false;
        }

        try {
            byte[] buffer = new byte[4];
            mOTARandomAccessFile.seek(0);
            int count = mOTARandomAccessFile.read(buffer, 0, 4);
            if (count < 4)
                return false;

            String magic = new String(buffer);
            if (magic.equalsIgnoreCase("AOTA")) {
                mOTARandomAccessFile.seek(64);
                byte[] buffer1 = new byte[32];
                count = mOTARandomAccessFile.read(buffer1, 0, 32);
                if (count < 32) {
                    Log.e("main_count_32", "count:" + count);
                    return false;
                }

                mFwVersion = new String(buffer1);
                Log.v(TAG, "======= count: " + count + " mFwVersion: " + mFwVersion);
            } else {
                mOTARandomAccessFile.seek(12);
                count = mOTARandomAccessFile.read(buffer, 0, 4);
                if (count < 4) {
                    Log.e("main_count_4", "count:" + count);
                    return false;
                }

                mFwVersion = new String(buffer);
                if (isStressTest) {
                    mFwVersion = String.format("%1.2f", mStressVersion);
                    mStressVersion += 0.01f;
                    if (mStressVersion > 9.99f) {
                        mStressVersion = 0.01f;
                    }
                }
                Log.v(TAG, "======= count: " + count + " mFwVersion: " + mFwVersion);

                mOTARandomAccessFile.seek(56);
                count = mOTARandomAccessFile.read(mVRAM, 0, 16);
                if (count < 16) {
                    Log.e("main_count_16", "count:" + count);
                    return false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    //    mFwVersion = fwVersion;
    //    mOTAMode = otaMode;
    //    mBatteryThreshold = batteryThreshold;

        return true;
    }
    // TODO
    private void handshake() {
        if (mListener == null)
            return;

        notifyStatus(STATE_PREPARING);

        boolean success = readOTAHeader();
        if (!success) {
            Log.e("amin_handShake", "!success");
            notifyStatus(STATE_UNKNOWN);
            return;
        }
        checkRemoteStatus(mFwVersion, mOTAMode, mBatteryThreshold);
        mSFRemoteTimeout = (RunnableScheduledFuture<Void>) mScheduledThreadPoolExecutor.schedule(mRemoteHandshakeTimeout, HANDSHAKE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private void startPull() {
        Log.d(TAG, "start() called. mRunning: " + mRunning);

//        mRunning = true;
//        pollThread.start();

        if (mPollThread != null) {
            Log.d(TAG, "start() thread state: " + mPollThread.getState());
        }

        if (mPollThread != null
                && mPollThread.getState() == Thread.State.RUNNABLE) {
            mRunning = true;
            return;
        }

        mRunning = true;
        mPollThread = new Thread(mProcessRunnable);
        mPollThread.start();
    }

    public String getOTAVersion() {

        if (mOTARandomAccessFile == null) {
            return null;
        }

        try {
            byte[] buffer = new byte[4];
            mOTARandomAccessFile.seek(0);
            int count = mOTARandomAccessFile.read(buffer, 0, 4);
            String magic = new String(buffer);
            if (magic.equalsIgnoreCase("AOTA")) {
                mOTARandomAccessFile.seek(64);
                byte[] buffer1 = new byte[32];
                count = mOTARandomAccessFile.read(buffer1, 0, 32);

                return new String(buffer1);
            } else {
                mOTARandomAccessFile.seek(12);
                count = mOTARandomAccessFile.read(buffer, 0, 4);

                return new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void prepare() {
        startPull();
        handshake();
    }

    public void upgrade() {
        try {
            notifyProgress(0, (int)mOTARandomAccessFile.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestRemoteParameters();
        Log.e("ota_upgrade", "grade");
        mSFRemoteTimeout = (RunnableScheduledFuture<Void>) mScheduledThreadPoolExecutor.schedule(mRemoteHandshakeTimeout, HANDSHAKE_TIMEOUT, TimeUnit.MILLISECONDS);
        mWriteBytes = 0;
    }

    public void cancel() {
        mRunning = false;
        notifyStatus(STATE_IDLE);
    }

    public void release() {
        if (mOTARandomAccessFile != null) {
            try {
            //    mOTAInputStream.close();
                mOTARandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyStatus(int state) {
        Log.e("notifystatus", state + "");
        if (mState == state)
            return;

        mState = state;
        if (mListener != null)
            mListener.onStatus(mState);
    }

    private void notifyProgress(int progress, int total) {
        Log.e("progress", "progress:" + progress + ";total:" + total);
        if (mListener != null)
            mListener.onProgress(progress, total);
    }

    private void notifyWriteBytes(int count) {
        if (mListener != null)
            mListener.onWriteBytes(count);
    }
/*
    private int getLength(IBluzIO io, int length) throws Exception {

        int buffer = (byte) io.read();
        int len = (length << 7) + (buffer & 0x7F);
        if ((buffer & 0x80) == 0) {
            return len;
        }

        return getLength(io, len);
    }
*/
    private ArrayList<byte[]> readFile(int offset, int length, byte[] bitmap, int groupNum) throws IOException {
        if (mOTARandomAccessFile == null) {
            return null;
        }

        int pkgIdx[] = BitmapUtils.getZeroBitIndexMap(bitmap, groupNum);

        int pkgNum = pkgIdx.length;
        Log.e("read_nick", "pkgNum:" + pkgNum);
        Log.e("nick_otaunit", mOtaUnit + ";");
        ArrayList packages = new ArrayList();
        for (int i = 0; i < pkgNum; i++) {
            int odd = length - pkgIdx[i] * mOtaUnit;
            Log.e("nick_odd", "odd:" + odd);
            if (odd <= 0)
                break;

            int o = offset + pkgIdx[i] * mOtaUnit;
            Log.e("nick_o", "o:" + o);
            int len = odd > mOtaUnit ? mOtaUnit : odd;
            Log.e("nick_len", "len:" + len);

            byte[] buffer = new byte[mOtaUnit];
            Log.e("nick_buffer", "buffer.len:" + buffer.length + ";mOtaUnit:" + mOtaUnit);
            mOTARandomAccessFile.seek(o);
            int count = mOTARandomAccessFile.read(buffer, 0, len);
            Log.v(TAG + "1", "i " + i + " o " + o + " pkgIdx " +  pkgIdx[i] + " pkgNum: " + pkgNum + " count: " + count);

            if (count <= 0)
                break;

            byte[] pkg = new byte[count];
            System.arraycopy(buffer, 0, pkg, 0, count);
            packages.add(pkg);
        }

        return packages;
    }

    private byte[] assembleTLV(int type, int length, byte[] value) {
        byte[] buffer = null;
        int index = 0;

        if (value == null) {
            buffer = new byte[1 + 2];
        } else {
            buffer = new byte[1 + 2 + value.length];
        }

        // Type
        buffer[index++] = (byte) type;

        // Length
        buffer[index++] = (byte) length;
        buffer[index++] = (byte) (length >> 8);

        // Value
        if (value != null && value.length > 0) {
            System.arraycopy(value, 0, buffer, index, value.length);
        }

        return buffer;
    }

    private byte[] assembleCommand(int serverID, int commandID, ArrayList<byte[]> subTLVs) {
        int subTLVsLen = 0;
        if (subTLVs != null && subTLVs.size() > 0) {
            for (byte[] tlv : subTLVs) {
                subTLVsLen += tlv.length;
            }
        }

        // Super TLV
        byte[] superTLV = assembleTLV(0x80, subTLVsLen, null);

        byte[] buffer = new byte[1 + 1 + superTLV.length + subTLVsLen];
        int index = 0;

        buffer[index++] = (byte) serverID;
        buffer[index++] = (byte) commandID;
        System.arraycopy(superTLV, 0, buffer, index, superTLV.length);
        index += superTLV.length;

        if (subTLVs != null && subTLVs.size() > 0) {
            for (byte[] tlv : subTLVs) {
                System.arraycopy(tlv, 0, buffer, index, tlv.length);
                index += tlv.length;
            }
        }

        return buffer;
    }

    private byte[] readSubTLVs(IBluzIO mIO) throws Exception {
        byte[] superTLV = new byte[3];
        int count = mIO.read(superTLV, 0, superTLV.length);
        Log.v(TAG, "readSubTLVs count:" + count);
        if (count < 3) {
            Log.e(TAG, "Un-handle Error occur(read super TLV)! Read count: " + count + "/3");
            Utils.printHexBuffer(TAG, superTLV);
            return null;
        }

        int subTLVsLen = superTLV[1] + (superTLV[2] << 8);
        Log.v(TAG, "subTLVsLen:" + subTLVsLen);
        byte[] tlvs = new byte[subTLVsLen];
        count = mIO.read(tlvs, 0, tlvs.length);
        Log.v(TAG, "count:" + count);
        if (count < subTLVsLen) {
            Log.e(TAG, "Un-handle Error occur(read sub TLV)! Read count: " + count + "/" + subTLVsLen);
            Utils.printHexBuffer(TAG, tlvs);
            return null;
        }

        return tlvs;
    }
    /*
    private ArrayList<byte[]> disperseSubTLV(byte[] buffer) {
        ArrayList<byte[]> tlvs = new ArrayList<>();


    }
*/
    private void checkRemoteStatus(String fwVersion, int mode, int batteryThreshold) {
        ArrayList<byte[]> tlvs = new ArrayList();
        byte[] tlv = null;

        // package version
        tlv = assembleTLV(0x01, fwVersion.length(), fwVersion.getBytes());
        tlvs.add(tlv);

        // component size
        byte[] valueSize = {0x00, 0x00};
        tlv = assembleTLV(0x02, 0x02, valueSize);
        tlvs.add(tlv);

        // VRAM
        tlv = assembleTLV(0x03, mVRAM.length, mVRAM);
        tlvs.add(tlv);

        // ota work mode
        byte[] valueMode = {(byte) mode};
        tlv = assembleTLV(0x04, 0x01, valueMode);
        tlvs.add(tlv);

        // feature support: Support CRC32 Checksum
        byte[] valueFeature = {(byte) 0x1};
        tlv = assembleTLV(0x09, 0x01, valueFeature);
        tlvs.add(tlv);

        byte[] buffer = assembleCommand(0x09, 0x01, tlvs);
        send(buffer);
    }

    private void requestRemoteParameters() {
        byte[] buffer = assembleCommand(0x09, 0x02, null);
        send(buffer);
    }

    private void notifyRemoteAppReady(int state) {
        ArrayList<byte[]> tlvs = new ArrayList();
        byte[] tlv = null;

        // ota status
        byte[] value = {(byte) state};
        tlv = assembleTLV(0x01, 0x01, value);
        tlvs.add(tlv);

        byte[] buffer = assembleCommand(0x09, 0x09, tlvs);
        send(buffer);
    }

    private final Runnable mRemoteHandshakeTimeout = new Runnable() {
        @Override
        public synchronized void run() {
            Log.e("main_timeout", "timeout");
            notifyStatus(STATE_UNKNOWN);
        }
    };

    class ProcessRunnable implements Runnable {
         @Override
         public void run() {
                 Log.v(TAG, "pollThread run: " + mRunning);
                 while (mRunning) {
                     try {
                         Log.e(TAG, "service");
                         int serviceId = mIO.read();

                         if (serviceId != 0x09) {
                             Log.v(TAG, "pollThread serviceId: " + serviceId);
                             continue;
                         }

                         int commandId = mIO.read();
                         Log.v(TAG, "pollThread run: service:" + serviceId + "; commandId:" + commandId);
                         Log.e(TAG, "curTime:" + System.currentTimeMillis());
                         switch (commandId) {

                             case 0x01: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null || tlvs.length < 7)
                                     continue;

                                 int index = 0;
                                 if (tlvs[index] != 0x7F)
                                     continue;

                                 index += 3;
                                 //    int errCode = (tlvs[index++] & 0xFF) | ((tlvs[index++] & 0xFF) << 8) | ((tlvs[index++] & 0xFF) << 16) | ((tlvs[index++] & 0xFF) << 24);
                                 int errCode = Utils.byteArrayToInt(tlvs, index);
                                 index += 4;

                                 boolean ret = mScheduledThreadPoolExecutor.remove(mSFRemoteTimeout);
                                 Log.v(TAG, "error code: " + errCode + ", remove ret: " + ret);

                                 RemoteStatus status = new RemoteStatus();

                                 for (; index < tlvs.length; ) {
                                     int type = tlvs[index++];
                                     int len = tlvs[index++];
                                     len += (((int) tlvs[index]) << 8);
                                     index++;
                                     Log.v(TAG, "index: " + index + ", type: " + type + ", len: " + len);
                                     switch (type) {
                                         case 0x04: // battery threshold
                                             status.batteryThreshold = tlvs[index++];
                                             break;
                                         case 0x05: // version name
                                             byte[] ver = new byte[len];
                                             System.arraycopy(tlvs, index, ver, 0, len);
                                             status.versionName = new String(ver);
                                             index += len;
                                             break;
                                         case 0x06: // board name
                                             byte[] board = new byte[len];
                                             System.arraycopy(tlvs, index, board, 0, len);
                                             status.boardName = new String(board);
                                             index += len;
                                             break;
                                         case 0x07: // hardware rev
                                             byte[] hardware = new byte[len];
                                             System.arraycopy(tlvs, index, hardware, 0, len);
                                             status.hardwareRev = new String(hardware);
                                             index += len;
                                             break;
                                         case 0x08: // version code
                                             status.versionCode = Utils.byteArrayToInt(tlvs, index);
                                             index += 4;
                                             break;
                                         case 0x09: // feature support
                                             status.featureSupport = tlvs[index++];
                                             remoteCrcSupport = (status.featureSupport & 0x01) == 0x01;
                                             break;
                                         default:
                                             index += len;
                                             break;
                                     }
                                 }


                                 if (errCode == 100000) {
                                     //    requestRemoteParameters();
                                     //    mSFRemoteTimeout = (RunnableScheduledFuture<Void>) mScheduledThreadPoolExecutor.schedule(mRemoteHandshakeTimeout, HANDSHAKE_TIMEOUT, TimeUnit.MILLISECONDS);
                                     mListener.onRemoteStatusReceived(status);

                                     notifyStatus(STATE_PREPARED);
                                 } else {
                                     if (mListener != null) {
                                         Log.v(TAG, "0x0901 Error: " + errCode);

                                         mListener.onError(errCode, ErrorCode.MESSAGE_UNKNOWN);
                                     }
                                 }
                                 break;
                             }

                             case 0x02: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null || tlvs.length < 24)
                                     continue;
                                 for (int i = 0; i < tlvs.length; i++) {
                                     Log.v("nick_02", "tlvs" + i + ":" + tlvs[i]);
                                 }
                                 // app wait timeout
                                 int index = 3;
                                 //    mWaitTimeout = tlvs[index++] + (tlvs[index++] << 8);
                                 mWaitTimeout = Utils.byteArrayToShort(tlvs, index);
                                 index += 2;

                                 // device restart timeout
                                 index += 3;
                                 //    mRestartTimeout = tlvs[index++] + (tlvs[index++] << 8);
                                 mRestartTimeout = Utils.byteArrayToShort(tlvs, index);
                                 index += 2;

                                 // ota unit size
                                 index += 3;
                                 //    mOtaUnit = tlvs[index++] + (tlvs[index++] << 8);
                                 mOtaUnit = Utils.byteArrayToShort(tlvs, index);
                                 Log.e("nick_02", "mOtaUnit:" + mOtaUnit);
                                 SharedPreferences sp = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
                                 SharedPreferences.Editor editor = sp.edit();
                                 editor.putInt("OTAUnit", mOtaUnit);
                                 editor.commit();
                                 index += 2;

                                 // interval
                                 index += 3;
                                 //    mInterval = tlvs[index++] + (tlvs[index++] << 8);
                                 mInterval = Utils.byteArrayToShort(tlvs, index);
                                 index += 2;

                                 // ack enable
                                 index += 3;
                                 mAckEnable = Utils.byteToInt(tlvs[index]);
                                 index++;

                                 mScheduledThreadPoolExecutor.remove(mSFRemoteTimeout);
                                 notifyRemoteAppReady(1);

                                 //    notifyStatus(STATE_PREPARED);
                                 break;
                             }

                             case 0x03: {
                                 // TODO
                                 if (mOtaUnit == 0) {
                                     Log.e("OTAManager_nick", "mOtaLen:" + mOtaUnit);
                                     SharedPreferences sp = mContext.getSharedPreferences("data", Context .MODE_PRIVATE);
                                     mOtaUnit = sp.getInt("OTAUnit", FRAME_SIZE);
                                     Log.e("OTAManager_nick", "mOtaLen:" + mOtaUnit);
                                 }
                                 Log.v(TAG + "nickk", "receive command 0x0903");

                                 notifyStatus(STATE_TRANSFERRING);

                                 byte[] tlvs = readSubTLVs(mIO);
                                 for (int i = 0; i < tlvs.length; i++) {
                                     Log.v(TAG + "nickk", "tlvs[" + i + "]: " + Integer.toBinaryString(tlvs[i]));
                                 }
                                 if (tlvs == null || tlvs.length < 14)
                                     continue;

                                 // 应答0903命令
                                 if (mAckEnable == 1) {
                                     byte[] buffer = new byte[5 + tlvs.length];
                                     buffer[0] = 0x09;
                                     buffer[1] = 0x03;
                                     buffer[2] = (byte) 0x80;
                                     buffer[3] = (byte) tlvs.length;
                                     buffer[4] = (byte) (tlvs.length >> 8);
                                     System.arraycopy(tlvs, 0, buffer, 6, tlvs.length);

                                     mIO.flush();
                                     mIO.write(buffer);
                                     mIO.flush();
                                 }

                                 // file offset
                                 int index = 3;
                                 //    int offset = tlvs[index++] + (tlvs[index++] << 8) + (tlvs[index++] << 16) + (tlvs[index++] << 24);
                                 int offset = Utils.byteArrayToInt(tlvs, index);
                                 index += 4;

                                 // file length
                                 index += 3;
                                 int length = Utils.byteArrayToInt(tlvs, index);
                                 index += 4;

                                 // file apply bitmap
                                 if (tlvs.length > 17) {
                                     index++;
                                     int len = Utils.byteArrayToShort(tlvs, index);
                                     Log.e("nickk", "len:" + len);
                                     index += 2;
                                     Log.v(TAG, "index:" + index);
                                     if (tlvs.length >= 17 + len) {
                                         byte[] bitmap = new byte[len];
                                         for (int i = 0; i < len; i++) {
                                             bitmap[i] = tlvs[index++];
                                             Log.v(TAG + "nickk", "bitmap[" + i + "]: " + Integer.toBinaryString(bitmap[i]) + ";index:" + index);
                                         }
                                         Log.e("nick_bitmap", bitmap.length + ";" + mOtaUnit);
                                         Log.e("nick_group", (length / mOtaUnit) + ";" + (length % mOtaUnit));
                                         int groupNum = length / mOtaUnit + 1;
                                         ArrayList<byte[]> frames = readFile(offset, length, bitmap, groupNum);
                                         if (frames == null) {
                                             Log.v(TAG, "OTA file does not exists!");
                                             continue;
                                         }

                                         for (int i = 0; i < frames.size(); i++) {
                                             int contentLen = frames.get(i).length;
                                             byte[] buffer;

                                             if (remoteCrcSupport) {
                                                 buffer = new byte[6 + 4 + contentLen];

                                                 buffer[0] = 0x09;
                                                 buffer[1] = 0x0B;
                                                 buffer[2] = (byte) 0x80;
                                                 buffer[3] = (byte) (contentLen + 1 + 4);
                                                 buffer[4] = (byte) ((contentLen + 1 + 4) >> 8);
                                                 buffer[5] = (byte) (i % 256);

                                                 CRC32 crc32 = new CRC32();
                                                 crc32.update(frames.get(i));
                                                 long checksum = crc32.getValue();
                                                 buffer[6] = (byte) checksum;
                                                 buffer[7] = (byte) (checksum >> 8);
                                                 buffer[8] = (byte) (checksum >> 16);
                                                 buffer[9] = (byte) (checksum >> 24);

                                                 System.arraycopy(frames.get(i), 0, buffer, 10, contentLen);
                                             } else {
                                                 buffer = new byte[6 + contentLen];

                                                 buffer[0] = 0x09;
                                                 buffer[1] = 0x04;
                                                 buffer[2] = (byte) 0x80;
                                                 buffer[3] = (byte) (contentLen + 1);
                                                 buffer[4] = (byte) ((contentLen + 1) >> 8);
                                                 buffer[5] = (byte) (i % 256);

                                                 System.arraycopy(frames.get(i), 0, buffer, 6, contentLen);
                                             }


                                             mIO.write(buffer);
                                             mIO.flush();
                                             mWriteBytes += buffer.length;
                                             Log.e("nick_write", mWriteBytes + ";" + buffer.length);
                                             notifyWriteBytes(mWriteBytes);
                                         }

                                         notifyProgress(offset + length, (int) mOTARandomAccessFile.length());
                                     }
                                 }
                                 break;
                             }

                             case 0x05: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null || tlvs.length < 14)
                                     continue;

                                 // package valid size
                                 int index = 3;
                                 int pkgValidSize = Utils.byteArrayToInt(tlvs, index);
                                 index += 4;

                                 // received file size
                                 index += 3;
                                 int receivedSize = Utils.byteArrayToInt(tlvs, index);
                                 index += 4;

                                 Log.v(TAG, "receive 0x0905 pkgValidSize: " + pkgValidSize + ", receivedSize:" + receivedSize);
                                 break;
                             }

                             case 0x06: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null || tlvs.length < 4)
                                     continue;

                                 int index = 3;
                                 int valid = Utils.byteToInt(tlvs[index]);
                                 index++;

                                 byte[] buffer = assembleCommand(0x09, 0x06, null);

                                 if (mListener != null) {
                                     if (valid == 1) {
                                         mListener.onStatus(STATE_TRANSFERRED);
                                     } else {
                                         mListener.onError(ErrorCode.PACKAGE_INVALID, ErrorCode.MESSAGE_PACKAGE_INVALID);
                                     }
                                 }

                                 mIO.flush();
                                 mIO.write(buffer);
                                 mIO.flush();

                                 break;
                             }

                             case 0x07: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null || tlvs.length < 7)
                                     continue;

                                 int index = 0;
                                 if (tlvs[index++] == 0x7F) {
                                     index += 2;
                                     int errCode = Utils.byteArrayToInt(tlvs, index);
                                     index += 4;
                                     if (errCode != 100000 && mListener != null) {
                                         Log.v(TAG, "0x0907 Error: " + errCode);

                                         mListener.onError(errCode, ErrorCode.MESSAGE_UNKNOWN);
                                     }
                                 }
                                 break;
                             }

                             case 0x7D: {
                                 byte[] tlvs = readSubTLVs(mIO);
                                 if (tlvs == null)
                                     continue;

                                 int index = 0;
                                 int type = Utils.byteToInt(tlvs[index]);
                                 index++;
                                 int len = Utils.byteArrayToShort(tlvs, index);
                                 index += 2;
                                 int psn = Utils.byteToInt(tlvs[index]);
                                 index++;
                                 Log.e("nick_7D", "0x097D, psn: " + psn + " len: " + len);

                                 type = Utils.byteToInt(tlvs[index]);
                                 index++;
                                 len = Utils.byteArrayToShort(tlvs, index);
                                 index += 2;

                                 byte[] buffer = new byte[len];
                                 System.arraycopy(tlvs, index, buffer, 0, len);


                                 Log.v(TAG, "Receive 0x097D, data size: " + len);
                                 Utils.printHexBuffer("Command 0x0907", buffer);

                                 if (mListener != null) {
                                     mListener.onAudioDataReceived(psn, len, buffer);
                                 }

                                 break;
                             }
                         }

                     } catch (Exception e) {
                         Log.w(TAG, "run Exception");
                         Log.w(TAG, e.getMessage());
                         handleException(e);
                     }
                     Log.v(TAG, "run command finish");
                 }
                 Log.v(TAG, "run exit!");

             //    });}
         }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        mRunning = false;
        Log.e(TAG, "handleException:" + e);
    }

 //   private void readBulkData(byte[] buffer) throws Exception {
        /**
         * FIXME read byte by byte to avoid the 256-boundary limit. if use the following interface, the data will be zero-padding to 256s mIO.read(buffer, 0,
         * length);
         */
 /*       for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) mIO.read();
        }

    }
*/
    private void send(byte[] commandBytes) {
        if (!mRunning) {
            return;
        }
        try {
            writeBuffer(commandBytes);
        } catch (Exception e) {
            handleException(e);
        }
    }


    /**
     * TODO notify data transfer fail
     */
  /*  private void notifyFail() {
        Log.e(TAG, "segment notifyFail");
    }


    private void tryWrite() throws Exception {
        writeBuffer();
        mScheduledThreadPoolExecutor.schedule(mSegmentTimeout, RETRY_INTERVALS, TimeUnit.MILLISECONDS);
    }

    private void writeBuffer() throws Exception {
        if (mLastBuffer != null)
            writeBuffer(mLastBuffer);
    }
*/
    private void writeBuffer(final byte[] buffer) {
        mScheduledThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mIO.flush();
                    mIO.write(buffer);
                    mIO.flush();
                } catch (Exception e) {
                    handleException(e);
                    e.printStackTrace();
                }
            }
        });
    }
}
