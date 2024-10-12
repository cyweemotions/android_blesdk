package com.actions.ibluz.device;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.arraycopy;

import com.tencent.mars.xlog.Log;

public class DataBuffer {
	public static abstract class WriteCallback {
		abstract void onStart();
	}

	public static class WriteDataBuffer {
		private static final int MAX_CMDS = 10;

		final Lock mLock = new ReentrantLock();

		ArrayList<byte[]> mItemList = new ArrayList<byte[]>();
		private WriteCallback mCallback;
		private byte[] mItem = null;
		private int mCount = 0;
		private int mOffset = 0;

        private static final int DEFAULT_WRITE_MAX_LENGTH = 20;
        private int writeMaxLength = DEFAULT_WRITE_MAX_LENGTH;
        private boolean isWriteMaxLengthChanged = false;
        private int tempWriteMaxLength = 20;


        public WriteDataBuffer(WriteCallback callback) {
			mItemList.clear();
			mCallback = callback;
		}

		public boolean add(byte[] buffer) {
			mLock.lock();
			int size = mItemList.size();
//			if (size >= MAX_CMDS) {
//				return false;
//			}

			mItemList.add(buffer);
			if (size == 0) {
				reload();
			}

			mLock.unlock();
			return true;
		}

        public void setWriteMaxLength(int newMaxLength) {
            isWriteMaxLengthChanged = true;
            tempWriteMaxLength = newMaxLength;
        }


        private void reload() {
			mItem = mItemList.get(0);
			mCount = mItem.length;
			mOffset = 0;
            if (isWriteMaxLengthChanged) {
                writeMaxLength = tempWriteMaxLength;
                isWriteMaxLengthChanged = false;
            }

            if (mCallback != null) {
				mCallback.onStart();
			}
		}

		public void next() {
			mLock.lock();
			mItemList.remove(0);
			if (mItemList.size() > 0) {
				reload();
			}
			mLock.unlock();
		}

		public byte[] getBuffer() {
			int length = (mCount <= writeMaxLength) ? mCount : writeMaxLength;
			Log.e("getBuffer", length + ";" + mCount + ";" + writeMaxLength);
			byte buffer[] = new byte[length];

			arraycopy(mItem, mOffset, buffer, 0, length);
			mCount -= length;
			mOffset += length;
			Log.e("getBuffer", "end:" + buffer.length);
			return buffer;
		}

		public boolean isEnd() {
			return (mCount == 0);
		}

		public boolean isStart() {
			return (mCount == mItem.length);
		}
	}

	public static class ReadDataBuffer {
		final Lock mLock = new ReentrantLock();
		final Condition mConditionEmpty = mLock.newCondition();

		ArrayList<byte[]> mItemList = new ArrayList<byte[]>();
		byte[] mItem = null;
		int mCount = 0;
		int mOffset = 0;

		byte[] mBuffer = null;
		int mBufferCount = 0;

		public ReadDataBuffer() {
			mItemList.clear();
		}

		private boolean reload() {
			mItemList.remove(mItem);
			if (mItemList.size() > 0) {
				mItem = mItemList.get(0);
				mCount = mItem.length;
				mOffset = 0;
				return true;
			} else {
				return false;
			}
		}

		public int read(byte[] buffer, int byteOffset, int byteCount) throws Exception {
			mLock.lock();
			try {
				/*
				 * packet may be divided into several sub packets, multiple reload need in this case.
				 */
				int dstOffset = 0;
				int byteLeft = byteCount;
				int srcOffset = byteOffset;

				Log.e("nick", "mCount:" + mCount + " byteLeft:" + byteLeft + " srcOffset:" + srcOffset);

				while (mCount < (byteLeft + srcOffset)) {
					int valid = mCount - srcOffset;
					if (valid > 0) {
						arraycopy(mItem, mOffset + srcOffset, buffer, dstOffset, valid);
						srcOffset = 0;
						dstOffset += valid;
						byteLeft -= valid;
					} else if (mCount > 0) {
						srcOffset -= mCount;
					}

					// load new Item
					while (!reload()) {
						mConditionEmpty.await();
					}
				}

				arraycopy(mItem, mOffset + srcOffset, buffer, dstOffset, byteLeft);
				mOffset += byteLeft;
				mCount -= byteLeft;
			} finally {
				mLock.unlock();
			}

			return byteCount;
		}

		public void add(int length) {
			mBuffer = new byte[length];
			mBufferCount = 0;
		}

		public void write(byte[] buffer) {
			int length = buffer.length;
			arraycopy(buffer, 0, mBuffer, mBufferCount, length);
			mBufferCount += length;
			if (mBufferCount >= mBuffer.length) {
				add();
			}
		}

		private void add() {
			mLock.lock();
			mItemList.add(mBuffer);
			mConditionEmpty.signal();
			mLock.unlock();
		}
	}

	public static class PacketInfo {
		public static final int TYPE_START = 0;
		public static final int TYPE_ING = 1;
		public static final int TYPE_END = 2;

		int mType;
		int mLength;

		public PacketInfo(byte[] buffer) {
			mLength = buffer[1] << 8 | buffer[0];
			mType = buffer[2];
		}

		public PacketInfo(int type, int length) {
			mType = type;
			mLength = length;
		}

		public byte[] toByteArray() {
			byte[] buffer = new byte[4];
			buffer[0] = (byte) (mLength & 0xff);
			buffer[1] = (byte) ((mLength >> 8) & 0xff);
			buffer[2] = (byte) mType;
			buffer[3] = 0;

			return buffer;
		}

		public int getType() {
			return mType;
		}

		public int getLength() {
			return mLength;
		}
	}
}
