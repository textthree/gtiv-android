package com.dqd2022.conn;

import com.dqd2022.Config;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import kit.LogKit;
import kit.NetworkKit;

public class ConnClient {
    public static volatile boolean socketThreadRunning;
    public static volatile SocketBlockingThread socketThread;
    //public volatile boolean stopSocketThread = false;

    // java 中 主线程无法访问子线程变量，
    // 如果主线程要访问子线程只能通过在主线程中调用 join 方法等待子线程结束，
    // 所以要停止子线程只有在主线程中设置 volatile 共享变量，socket 子线程中通过读取标志位终止阻塞自然停止线程
    public void start() {
        if (!NetworkKit.checkNetworkAvailable(App.context)) {
            LogKit.p("网络不可用");
            return;
        }
        socketThreadRunning = true;
        socketThread = new SocketBlockingThread();
        socketThread.setUncaughtExceptionHandler(new RecoverThread());
        socketThread.start();
    }

    // 断开连接
    public void close() {
        socketThreadRunning = false;
    }

    // 主动检查 socket 状态
    public void checkState() {
        if (!NetworkKit.checkNetworkAvailable(App.context)) return;
        if (socketThread == null) {
            LogKit.p("[checkState]", "socketThread is null");
            return;
        }
        LogKit.p("[socketThread.isRunning()]", socketThread.isRunning());
        LogKit.p("[socketThread.isConnected()]", socketThread.isConnected());
        LogKit.p("[socketThread.trySendStream()]", socketThread.trySendStream());
        LogKit.p("[socketThread.isHeartBeatWriteSuccess()]", socketThread.isHeartBeatWriteSuccess());
    }

    // 重新建立连接
    public void reconnect() {
        LogKit.p("connClient.stopSocket();");
        socketThread.stopSocket();
        // TODO 建立连接后重新拉一次消息，防止建连过程中丢消息
        ImHelpers.getMessageFromServer();
    }

    // 线程发生异常时进行恢复
    private class RecoverThread implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LogKit.p("长连接线程异常，三秒后重新启动线程");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            // 启动新线程
            socketThread = new SocketBlockingThread();
            socketThread.setUncaughtExceptionHandler(new RecoverThread());
            socketThread.start();
        }
    }

}
