package com.desaysv.sceneengine.socket;

import static com.desaysv.aisound.BaseApplication.BASE_TAG;

import android.os.Handler;
import android.util.Log;

import com.desaysv.sceneengine.thread.ThreadDispatcher;
import com.desaysv.sceneengine.util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class SocketClient {
    private static final String TAG = BASE_TAG + "SocketClient";
    private Socket socketClient;

    private int tryConnectTimes = 0;

    private OutputStream outputStream;
    private InputStream inputStream;
    private static volatile SocketClient instance;
    private Handler handler;
    public static boolean isLogined = false; //是否已登录
    public static boolean isRunningHeart = false; //是否已开启心跳包
    public static boolean isLogining = false; //已发登录请求，但还未收到结果
    public static long lastRequestTime = 0; //上次请求的时间
    private static final byte BYTE_START = 0x21; // 包头
    private static final byte BYTE_END_1 = 0x0D;
    private static final byte BYTE_END_2 = 0x0A; // 包尾
    private byte[] mSubByte = null; // 记录上一次未处理的Byte

    private static boolean isOpen = false;

    private SocketClient() {
        handler = new Handler();
    }

    public static SocketClient getInstance() {
        if (instance == null) {
            synchronized (SocketClient.class) {
                if (instance == null) {
                    instance = new SocketClient();
                }
            }
        }
        return instance;
    }

    public void open(boolean on) {
        isOpen = on;
    }

    public void initClient() {
        if(!isOpen) return;
        ThreadDispatcher.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: initClient");
                try {
                    Log.d(TAG, "Attempting to connect to server...");
                    String[] serverAddresses = new String[]{"192.168.10.53"};
                    int port = 30002;
                    int connectionTimeout = 5000;
                    int readTimeout = 10000;

                    Log.d(TAG, "Using configuration - Port: " + port +
                            ", Connection Timeout: " + connectionTimeout +
                            ", Read Timeout: " + readTimeout);
                    Log.d(TAG, "Server addresses: " + Arrays.toString(serverAddresses));

                    boolean connected = false;
                    Exception lastException = null;

                    for (String address : serverAddresses) {
                        try {
                            Log.d(TAG, "Trying to connect to " + address + ":" + port);
                            socketClient = new Socket();
                            // 设置socket选项
                            socketClient.setTcpNoDelay(true);
                            socketClient.setKeepAlive(true);
                            socketClient.setSoTimeout(readTimeout);

                            // 尝试连接
                            Log.d(TAG, "Connecting to " + address + "...");
                            socketClient.connect(new InetSocketAddress(address, port), connectionTimeout);

                            if (socketClient.isConnected()) {
                                Log.d(TAG, "Successfully connected to " + address + ":" + port);
                                connected = true;
                                break;
                            }
                        } catch (Exception e) {
                            lastException = e;
                            Log.e(TAG, "Failed to connect to " + address + ": " + e.getMessage());
                            Log.e(TAG, "Stack trace: ", e);
                            try {
                                if (socketClient != null) {
                                    socketClient.close();
                                }
                            } catch (Exception closeEx) {
                                Log.e(TAG, "Error closing socket: " + closeEx.getMessage());
                            }
                        }
                    }

                    if (!connected) {
                        String errorMsg = "Could not connect to any server address. Last error: " +
                                (lastException != null ? lastException.getMessage() : "Unknown error");
                        Log.e(TAG, errorMsg);
                        throw new IOException(errorMsg);
                    }

                    // 设置输入输出流
                    Log.d(TAG, "Setting up streams...");
                    outputStream = socketClient.getOutputStream();
                    inputStream = socketClient.getInputStream();
                    Log.d(TAG, "Streams initialized successfully");
                    //SocketAnalysis.getInstance().analysisMsgToPC(SocketConstants.Request.ORDER_LOGIN);
                    // 从输入流启动接收
                    //receiveMsgFromPc();
                } catch (IOException e) {
                    Log.e(TAG, "run: IOException: " + e.toString());
                    reLinkPC();
                }
            }
        });
    }

    private void reLinkPC() {
        if(tryConnectTimes++ > 3) return;
        Log.d(TAG, "reLinkPC: ");
        initClient();
    }

    /**
     * 将dest添加到src后， src + dest
     *
     * @param src  原始数组
     * @param dest 目标数组
     * @return 封装后的数组
     */
    private static byte[] addBytes(byte[] src, byte[] dest) {
        if (src == null) {
            return dest;
        }
        if (dest == null) {
            return src;
        }
        byte[] result = new byte[src.length + dest.length];
        for (int i = 0; i < result.length; ++i) {
            if (i < src.length) {
                result[i] = src[i];
            } else {
                result[i] = dest[i - src.length];
            }
        }
        return result;
    }

    /**
     * 在src中找到第一个data的位置
     *
     * @param src
     * @param data
     * @return 返回data的位置
     */
    private static int indexOf(byte[] src, byte data) {
        int ret = -1;
        for (int i = 0; i < src.length; ++i) {
            if (src[i] == data) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /**
     * 在src中找到第一个dest
     *
     * @param src
     * @param dest1
     * @param dest2
     * @return
     */
    private static int indexOf(byte[] src, byte dest1, byte dest2) {
        int ret = -1;
        for (int i = 0; i < src.length - 1; ++i) {
            if (src[i] == dest1 && src[i + 1] == dest2) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /**
     * 从byte[] srcs 里面截取 start 开头， 长度为len的数组
     *
     * @param srcs
     * @param start
     * @param len
     * @return
     */
    private static byte[] subBytes(byte[] srcs, int start, int len) {
        if (null == srcs) return new byte[0];
        if (start >= srcs.length || len < 0) return new byte[0];
        if (start < 0) start = 0;
        if (start + len > srcs.length) len = srcs.length - start;
        byte[] result = new byte[len];
        System.arraycopy(srcs, start, result, 0, len);
        return result;
    }

    /**
     * 解析数据
     *
     * @param
     * @return
     */
    private boolean analysisByte() {
        int start = indexOf(mSubByte, BYTE_START);
        if (start == -1) { // 无数据
            mSubByte = null;
            return false;
        }
        int end = indexOf(mSubByte, BYTE_END_1, BYTE_END_2);
        if (end == -1) { // 有剩余数据，但是没有结束符，该数据缓存到 mSubByte中，等待下次使用
            return false;
        } else {
            byte[] ret = subBytes(mSubByte, start, (end + 2 - start));
            Log.d(TAG, "run receiveMsgFromPc. msg:" + Arrays.toString(ret));
            SocketAnalysis.getInstance().processMsgFromPC(ret); // 处理数据

            // 截取数据，下一次使用
            mSubByte = subBytes(mSubByte, end + 2, mSubByte.length);
            Log.d(TAG, "run receiveMsgFromPc. process package data. less data:" + Arrays.toString(mSubByte));
            return true;
        }
    }

    private void receiveMsgFromPc() {
        ThreadDispatcher.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run receiveMsgFromPc.");
                byte[] readArr = new byte[256];
                byte[] ret;
                int length = 0;

                try {
                    while (true) {
                        if (inputStream == null || socketClient == null || socketClient.isClosed()) {
                            Log.e(TAG, "Connection is not available");
                            break;
                        }

                        // 检查是否有数据可读
                        if (inputStream.available() <= 0) {
                            Thread.sleep(5);  // 短暂休眠避免CPU占用过高
                            continue;
                        }

                        length = inputStream.read(readArr);
                        if (length == -1) {
                            Log.e(TAG, "Connection closed by server");
                            break;
                        }
                        Log.d(TAG, "run receiveMsgFromPc. length:" + length + ",data:" + Arrays.toString(readArr));

                        ret = subBytes(readArr, 0, length); // 只拿有效数据
                        Log.d(TAG, "run receiveMsgFromPc. process valid data. length:" + length + ",data:" + Arrays.toString(ret));
                        mSubByte = addBytes(mSubByte, ret); // 和上一次剩余数据进行组包
                        Log.d(TAG, "run receiveMsgFromPc. process package data. length:" + mSubByte.length + ",data:" + Arrays.toString(mSubByte));
                        if (mSubByte == null) {
                            continue;
                        }
                        while (analysisByte()) ;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run receiveMsgFromPc. IOException:" + e.toString());
                    //reLinkPC();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

//                byte[] readArr = new byte[8];
//                byte[] cacheArr = new byte[8];
//                int cacheLength = 0;
//                int length = 0;
//                try {
//                    while ((length = inputStream.read(readArr)) != -1) {
//                        Log.d(TAG, "run: length:" + length + " receiveMsgFromPc:" + Arrays.toString(readArr));
//                        if (readArr[0] == 33) {
//                            Log.d(TAG, "run receiveMsgFromPc. final:" + Arrays.toString(readArr));
//                            SocketAnalysis.getInstance().analysisMsgFromPC(readArr);
//                            if (length == 8 && readArr[7] == 33) {
//                                cacheArr[0] = 33;
//                                cacheLength = 1;
//                            }
//                        } else {
//                            if (cacheArr[0] == 33 && cacheLength > 0) {
//                                if (length == 8) {
//
//                                } else {
//                                    for (int i = 0; i < readArr.length; i++) {
//                                        cacheArr[cacheLength] = readArr[i];
//                                        cacheLength++;
//                                    }
//                                    Log.d(TAG, "run receiveMsgFromPc. final:" + Arrays.toString(cacheArr));
//                                    SocketAnalysis.getInstance().analysisMsgFromPC(cacheArr);
//                                    cacheLength = 0;
//                                    cacheArr[0] = 0;
//                                }
//                            }
//
//                            if (cacheArr[0] == 33 && cacheLength > 0) {
//                                for (int i = 0; i < readArr.length; i++) {
//                                    cacheArr[cacheLength] = readArr[i];
//                                    cacheLength++;
//                                    if (readArr[i] == 10) {
//                                        if (cacheLength > 6 && cacheArr[cacheLength-2] == 13) {
//                                            Log.d(TAG, "run receiveMsgFromPc. final:" + Arrays.toString(cacheArr));
//                                            SocketAnalysis.getInstance().analysisMsgFromPC(cacheArr);
//                                            if (length - 1 > i) {
//                                                if (readArr[i + 1] == 33) {
//                                                    cacheLength = 0;
//                                                } else {
//                                                    cacheLength = 0;
//                                                    cacheArr[0] = 0;
//                                                    break;
//                                                }
//                                            } else {
//                                                cacheLength = 0;
//                                                cacheArr[0] = 0;
//                                            }
//                                        }
//                                    }
//                                    if (cacheLength == 8) {
//                                        Log.d(TAG, "run receiveMsgFromPc. final:bad data");
//                                        cacheLength = 0;
//                                        cacheArr[0] = 0;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    Log.d(TAG, "run2: receiveMsgFromPc final:" + Arrays.toString(readArr));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "run: IOException:" + e.toString());
//                    reLinkPC();
//                }
            }
        });
    }

    public void postMsgToPc(byte[] data) {
        if(!isOpen) return;
        //Log.d(TAG, "postMsgToPc");
        ThreadDispatcher.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run postMsgToPc.");
                if (outputStream != null) {
                    try {
                        outputStream.write(data);
                        outputStream.flush();
                        lastRequestTime = System.currentTimeMillis();
                        Log.d(TAG, "run postMsgToPc. data:" + ByteUtil.bytesToHexWithPrefix(data));
                    } catch (IOException e) {
                        Log.e(TAG, "run postMsgToPc. IOException:" + e.toString());
                        reLinkPC();
                    }
                } else {
                    Log.d(TAG, "outputStream == null");
                    reLinkPC();
                }
            }
        });
    }

    public void heartbeat() {
        Log.d(TAG, "heartbeat: start");
        while (true) {
            isRunningHeart = true;
            if (isLogined) {
                Log.d(TAG, "heartbeat: send to server");
                SocketAnalysis.getInstance().analysisMsgToPC(SocketConstants.Request.ORDER_HEARTBEAT);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "heartbeat: e:" + e);
                    heartbeat();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
