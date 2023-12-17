package com.dqd2022.conn;

import com.alibaba.fastjson.JSONObject;
import com.dqd2022.Config;
import com.dqd2022.api.Base;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import kit.LogKit;

public class SocketBlockingThread extends Thread {

    private enum State {
        STOPPED, STOPPING, RUNNING, CLOSE
    }

    public volatile boolean heartBeatWriteSuccess = true;
    public volatile boolean hearBeatEnabled = true;   // 心跳开关，退出登录时停止心跳检查
    private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);
    InetAddress server;
    protected int port;
    private int defaultBufferSize = 4096; // 单条消息最大包长度，跟后端定的协议对应
    private int heartBeatInterval = 30 * 1000;
    private int checkCometInterval = 6 * 60 * 1000; // 5 分钟没有收到心跳 comet 会主动断开，所以 checkComet 大于五分钟
    private int defaultSocketTimeOut = 3 * 60 * 1000;
    private final AtomicReference<DataOutputStream> out = new AtomicReference<DataOutputStream>();
    private final AtomicReference<DataInputStream> in = new AtomicReference<DataInputStream>();
    Socket socket;


    @Override
    public void run() {
        String[] comet = kit.StringKit.expload(":", Config.COMET_SERVER);
        String server = comet[0];
        this.port = kit.StringKit.parseInt(comet[1]);
        InetAddress address;
        try {
            address = InetAddress.getByName(server);
        } catch (UnknownHostException e) {
            LogKit.p("网关地址解析错误");
            e.printStackTrace();
            return;
        }
        this.server = address;
        blocking();
    }

    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }

    public boolean isConnected() {
        if (socket == null) return false;
        return socket.isConnected();
    }

    // 尝试发送数据，如果连接不可用将抛出异常，用于检测连接是否可用
    public boolean trySendStream() {
        if (socket == null) return false;
        try {
            socket.getOutputStream();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 客户端心跳是否失败了
    public boolean isHeartBeatWriteSuccess() {
        return heartBeatWriteSuccess;
    }

    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    /**
     * 连接到服务端，如果已经连接上则不会重复连接
     * 此连接运行在自己的线程中(ConnClient.java 中开的线程)，直到客户端退出才会终止。
     *
     * @throws RuntimeException if the client fails
     */
    public void blocking() {
        socket = null;
        try {
            socket = new Socket(server, port);
            socket.setSoTimeout(defaultSocketTimeOut);

            // 创建输入流，接收服务端发来数据
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            DataInputStream dataInputStream = new DataInputStream(inputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            in.set(dataInputStream);
            out.set(dataOutputStream);

            if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
                LogKit.p("当前不是 STOPPED 状态, 禁止建立新的连接");
                return;
            }

            authWrite();

            // 读取流
            while (ConnClient.socketThreadRunning) {
                byte[] inBuffer = new byte[defaultBufferSize];
                int readPoint = in.get().read(inBuffer);  // 产生阻塞，直到读取内容为止
                // readPoint 是消息长度，不等于 -1 代表有消息来了
                if (readPoint != -1) {
                    // 前 16 为协议信息，使用字节序记录消息体长度、操作、消息版本等信息，是后端 job 服务追加的，除去这 16 个才是消息体的长度
                    // 要分包，当服务端高频率写数据时，客户端收到的数据流是多条消息拼接在一起的，需要客户端自行分开
                    int messageBodyLength = inBuffer.length - 16;
                    byte[] originMessage = new byte[messageBodyLength];
                    if (inBuffer.length < messageBodyLength) {
                        originMessage = null;
                    }

                    // System.arraycopy(inBuffer, inBuffer.length - messageBodyLength, originMessage, 0, messageBodyLength);
                    // String str = new String(originMessage).trim();
                    // int originPackLen = str.length() + 16;
                    // LogKit.p("未拆包的消息，长度：" + originPackLen + "，内容：" + str);

                    int processedLength = 0;
                    while (processedLength < defaultBufferSize) {
                        Long operation = BruteForceCoding.decodeIntBigEndian(inBuffer, 8, 4);
                        if (operation == 3) {
                            // 接收服务端心跳
                            LogKit.p("heartBeatReceived...");
                            break;
                        } else if (8 == operation) {
                            // 鉴权成功
                            LogKit.p("authSuccess...");
                            heartBeat();
                            checkCometThread();
                            break;
                        } else {
                            // 协议设计：http://text3.cn/blog-343333541.html#4ji77r
                            // Long packageLength = BruteForceCoding.decodeIntBigEndian(inBuffer, 0, 4);
                            // Long headLength = BruteForceCoding.decodeIntBigEndian(inBuffer, 4, 2);
                            // Long version = BruteForceCoding.decodeIntBigEndian(inBuffer, 6, 2);
                            // Long sequenceId = BruteForceCoding.decodeIntBigEndian(inBuffer, 12, 4);
                            int offset = processedLength > 0 ? processedLength : 0;
                            long msgLen = BruteForceCoding.decodeIntBigEndian(inBuffer, offset, 4);
                            if (msgLen == 0) {
                                break;
                            }
                            byte[] message = new byte[(int) msgLen];
                            // arraycopy 函数参数详解：
                            // 第一个参数: 源数组
                            // 第二个参数：从原数组的指定下标处开始复制
                            // 第三个参数：目标数组
                            // 第四个参数：在目标数组中，从第几个下标开始放入复制的数据
                            // 第五个参数：在源数组中，一共拿几个数值放到目标数组中
                            System.arraycopy(inBuffer, processedLength + 16, message, 0, (int) msgLen - 16);
                            processedLength += (int) msgLen;
                            String msgBody = new String(message).trim();
                            new ReciveMessage(msgBody, operation);
                        }
                    }
                }
            }
        } catch (Exception ioe) {
            LogKit.p("解析消息失败：" + ioe.getMessage());
            ioe.printStackTrace();
            if (state.get() != State.CLOSE) reConnect();
        }
        LogKit.p("解除阻塞，自然结束 Socket 线程");
        stopSocket();
    }

    protected void reConnect() {
        if (ConnClient.socketThreadRunning) {
            try {
                LogKit.p("[reConnect] 8 秒后重新建立连接");
                Thread.sleep(8000);
                socket.close();
                state.set(State.STOPPED);
            } catch (Exception e) {
                // 断开连接失败
                LogKit.p("[reConnect]", "断开连接失败");
            }
            run();
        }
    }


    /**
     * 以优雅的方式停止客户端，停止过程可能需要一点时间
     *
     * @return 是否成功停止
     */
    public boolean stopSocket() {
        LogKit.p("[stopSocket] 断开连接");
        if (state.compareAndSet(State.RUNNING, State.STOPPING)) {
            try {
                in.get().close();
            } catch (IOException e) {
                return false;
            }
            return true;
        } else {
            LogKit.p("[stopSocket] 断开连接失败");
        }
        return false;
    }


    public void close() {
        LogKit.p("[close] 关闭连接");
        state.set(State.CLOSE);
        stopSocket();
    }


    /**
     * 发送连接鉴权消息
     * <p>
     * param buffer the message to send.
     *
     * @return true if the message was sent to the server.
     * @throws IOException
     */
    public synchronized Boolean authWrite() throws IOException {
        // String msg = "{'mid':216, 'room_id':'room_5', 'platform':'android', 'accepts':[{5, 6, 7}]}";
        // mid 用于接收个人消息，logic -> PushRoom
        // room_id 用于接收某个房间的消息，logic -> PushRoom
        // accepts 用于接收全局广播，logic -> PushAll
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userToken", App.token);
        jsonObject.put("platform", "android");
        //jsonObject.put("room_id", "0");  // 单房间场景
        String msg = jsonObject.toString();
        LogKit.p(msg);

        int packLength = msg.length() + 16;
        byte[] message = new byte[4 + 2 + 2 + 4 + 4];

        // package length
        int offset = BruteForceCoding.encodeIntBigEndian(message, packLength, 0, 4 * BruteForceCoding.BSIZE);
        // header lenght
        offset = BruteForceCoding.encodeIntBigEndian(message, 16, offset, 2 * BruteForceCoding.BSIZE);
        // ver
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 2 * BruteForceCoding.BSIZE);
        // operation
        offset = BruteForceCoding.encodeIntBigEndian(message, 7, offset, 4 * BruteForceCoding.BSIZE);
        // jsonp callback
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 4 * BruteForceCoding.BSIZE);

        out.get().write(BruteForceCoding.add(message, msg.getBytes()));
        out.get().flush();

        return true;

    }


    /**
     * 开启发送心跳到服务端的线程，当建立连接成功后触发
     */
    private void heartBeat() {
        // 通过 socket 发心跳包告诉服务端我还在线，但是弱网环境导致心跳发送失败那服务端就断开了
        class HeartbeatTask implements Runnable {
            @Override
            public void run() {
                // !Thread.currentThread().isInterrupted()
                while (hearBeatEnabled) {
                    try {
                        Thread.sleep(heartBeatInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        heartBeatWrite();
                    } catch (IOException e) {
                        LogKit.p("客户端心跳发现 Socket 关闭了，尝试：ImHelpers.connClient = new ConnClient();");
                        heartBeatWriteSuccess = false;
                        ImHelpers.connClient = new ConnClient();
                        e.printStackTrace();
                    }
                }
            }
        }
        Thread sendHeartBeatPackage = new Thread(new HeartbeatTask());
        sendHeartBeatPackage.start();
    }

    /**
     * 发送心跳消息到服务端使用同步方法
     */
    public synchronized Boolean heartBeatWrite() throws IOException {
        String msg = App.myUserId + "_";

        int packLength = msg.length() + 16;
        byte[] message = new byte[4 + 2 + 2 + 4 + 4];

        // package length
        int offset = BruteForceCoding.encodeIntBigEndian(message, packLength, 0, 4 * BruteForceCoding.BSIZE);
        // header lenght
        offset = BruteForceCoding.encodeIntBigEndian(message, 16, offset, 2 * BruteForceCoding.BSIZE);
        // ver
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 2 * BruteForceCoding.BSIZE);
        // operation
        offset = BruteForceCoding.encodeIntBigEndian(message, 2, offset, 4 * BruteForceCoding.BSIZE);
        // jsonp callback
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 4 * BruteForceCoding.BSIZE);

        out.get().write(BruteForceCoding.add(message, msg.getBytes()));
        out.get().flush();

        return true;
    }


    /**
     * 双重保险，通过 http 服务主动查询 comet 状态，不在线则重连
     */
    private void checkCometThread() {
        new Thread(() -> {
            while (hearBeatEnabled) {
                try {
                    Thread.sleep(checkCometInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    checkComet();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    // 通过 http 服务检查与 comet 的连接状态，
    // 一个类中的所有同步方法是互斥的，也就是说两个同步方法即使在两个线程中也不会同时执行
    // 因为类中的所有同步方法使用的是同一把锁: this
    public synchronized void checkComet() throws IOException {
        LogKit.p("checkCommet...");
        if (!Base.checkComet()) {
            LogKit.p("[checkCommet] 重新建立连接");
            reConnect();
        }
    }

}