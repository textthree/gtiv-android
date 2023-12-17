package com.dqd2022.page.webrtc;

import com.dqd2022.constant.WebrtcSocketIoEvent;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import kit.LogKit;

public class SocketIO {
    private Socket socket;
    private Callback callback;
    private Boolean connected = false;
    public String mySocketId;

    public SocketIO(String uri) {
        try {
            socket = IO.socket(uri);
        } catch (URISyntaxException e) {
        }
        socket.connect();
        socket.on(Socket.EVENT_CONNECT, onConect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        socket.on(WebrtcSocketIoEvent.message.name(), onMessage);
        socket.on(WebrtcSocketIoEvent.joined.name(), onJoined);
    }

    // fragment onDestroy() 中调用销毁
    public void destroy() {
        LogKit.p("[socketIO] destroy");
        if (socket != null) {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT);
            socket.off(Socket.EVENT_DISCONNECT);
            socket.off(Socket.EVENT_CONNECT_ERROR);
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
            socket.off(WebrtcSocketIoEvent.message.name(), onMessage);
            socket.off(WebrtcSocketIoEvent.joined.name(), onJoined);
        }
    }

    // 由实例中实现接口方法传递过来
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onConnect();

        void onMessage(String text);
    }

    private Emitter.Listener onJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LogKit.p("[socketIO onJoined]: sockdetId " + args[0]);
            mySocketId = args[0].toString();
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            String fromSocketId = args[0].toString();
            String msg = args[1].toString();
            // 过滤掉自己发送的房间消息
            if (!fromSocketId.equals(mySocketId)) {
                callback.onMessage(msg);
            }
            // LogKit.p("[socketIO onMessage args1]: " + args[0]);
            // LogKit.p("[socketIO onMessage args2]: " + args[1]);
        }
    };

    // 进入房间
    public void join(WebrtcSocketIoEvent event, String roomId) {
        if (socket != null && connected) {
            socket.emit(event.name(), roomId);
        } else {
            LogKit.p("[socketIO] 加入房间失败，socket 未连接");
        }
    }

    // 发送消息到房间
    public void send(WebrtcSocketIoEvent event, String roomId, String message) {
        if (socket != null && connected) {
            socket.emit(event.name(), roomId, message);
            LogKit.p("[socketIO send " + event + " to " + roomId + "]: " + message);
        } else {
            LogKit.p("[socketIO] 发送失败，socket 未连接");
        }
    }


    private Emitter.Listener onConect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LogKit.p("[socketIO] connected");
            connected = true;
            callback.onConnect();
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LogKit.p("[socketIO] diconnected");
            connected = false;
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LogKit.p("[socketIO] error");
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            LogKit.p("[socketIO] timeout");
        }
    };


}

