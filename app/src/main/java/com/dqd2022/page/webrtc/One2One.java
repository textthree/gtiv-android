package com.dqd2022.page.webrtc;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.dqd2022.Config;
import com.dqd2022.R;
import com.dqd2022.constant.CallAction;
import com.dqd2022.constant.CallType;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.MessageType;
 
import com.dqd2022.constant.WebrtcSocketIoEvent;
import com.dqd2022.helpers.ImSendMessageHelper;
import com.dqd2022.helpers.SendPrivateMessageReqDto;


import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Date;
import java.util.LinkedList;

import kit.LogKit;
import kit.StringKit;

// 参考自：https://github.com/lesliebeijing/WebRtcDemo/blob/master/app/src/main/java/com/lesliefang/webrtcdemo/CallActivity.java
// 谷歌官方实例代码：https://webrtc.googlesource.com/src/+/refs/heads/master/examples
// https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/examples/androidapp/src/org/appspot/apprtc?autodive=0/
public class One2One {
    private int fps = 20; // 视频帧率
    private String signallingRoomId;
    private int callAction; // 是打电话还是接电话
    private int callType;   // 是视频还是音频通话
    private Activity activity;
    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;
    private static final String VIDEO_TRACK_ID = "1"; // ARDAMSv0
    private static final String AUDIO_TRACK_ID = "2"; // ARDAMSa0
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private EglBase.Context eglBaseContext;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private SurfaceTextureHelper surfaceTextureHelper;
    private SurfaceViewRenderer surfaceBig, surfaceSmall;
    private SocketIO socketIO;
    private final String WEBSOCKET_URL = Config.SIGNAL_SERVER;
    private int bizId;

    public One2One(Activity activity, int localUser, int remoteUser, int action, int type) {
        bizId = remoteUser;
        this.activity = activity;
        this.callAction = action;
        this.callType = type;
        switch (action) {
            case CallAction.CALL:
                this.signallingRoomId = localUser + "_" + remoteUser;
                break;
            case CallAction.ANSWER:
                this.signallingRoomId = remoteUser + "_" + localUser;
                break;
        }
        localInit();
        // 初始化完成后再建立 socket 发送 offer，时序不能颠倒，否则绑定不上流
        socketIO = new SocketIO(WEBSOCKET_URL + "/one2one");
        socketIO.setCallback(socketCallback);
    }


    /**
     * 初始化本地工作
     */
    public void localInit() {
        // 创建音视频源（Stream 流）和音视频轨（Track 轨），然后将源（通过采集设备获取）和轨关联起来
        // 要注意动态请求录音权限，否则没声音
        peerConnectionFactory = createPeerConnectionFactory();
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        if (callType == CallType.Video) {
            // 对于音频来说，在创建 AudioSource 时就开始从默认的音频设备捕获音频数据了；
            // 而对于视频来说，还需要指定采集视频数据的设备，然后通过观察者模式从指定设备中获取数据。
            // VideoCapturer 与 VideoSource 是通过 VideoCapturer 的 initialize（）函数关联到一起的。
            videoCapturer = createVideoCapturer();
            if (videoCapturer == null) {
                LogKit.p("createVideoCapturer fail");
                return;
            }
            // EGL 是 OpenGL ES 与 SurfaceViewRenderer 之间的桥梁，它可以调用 OpenGL ES 渲染视频，再将结果显示到 SurfaceViewRenderer
            eglBaseContext = EglBase.create().getEglBaseContext();
            videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
            // initialize() 函数的三个参数：
            //  第一个参数: Android 系统中，必须为 Camera 设置一个 Surface，这样它才能开启摄像头，并从摄像头中获取视频数据。
            //  第二个参数: 用于获取与应用相关的数据。
            //  第三个参数: 其作用是 Capturer 的观察者，VideoSource 可以通过它从 Capturer 获取视频数据。
            videoCapturer.initialize(surfaceTextureHelper, activity, videoSource.getCapturerObserver());
            // 打开摄像头采集视频流，三个参数分别为：宽、高、帧率（FPS）
            // 采集的分辨率一定要符合 16:9 或 9:16 或 4:3 或 3:4 这样的比例，否则在渲染时很可能会出现问题，如绿边等。
            // 对于实时通信场景来说，一般帧率都不会设置得太高，通常 15 帧就可以满足大部分的需求。
            videoCapturer.startCapture(1280, 720, fps);
            // 通过 SurfaceViewRenderer 将采集到的影像渲染到控件上
            surfaceBig = activity.findViewById(R.id.video_call_surface_big);
            surfaceBig.init(eglBaseContext, null);
            // 设置影像的填充模式，SCALE_ASPECT_FILL 表示将视频按比例填充到 View 中
            surfaceBig.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            // 翻转视频，因为采集的视频图像与我们眼睛看到的是相反的，所以给他反过来才像是照镜子
            surfaceBig.setMirror(true);
            // 关闭硬件视频拉伸功能
            surfaceBig.setEnableHardwareScaler(false);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            // 保持屏幕常亮，即禁止锁屏
            surfaceBig.setKeepScreenOn(true);
            // 将视频流与 View 绑定
            localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            // 拨打方直接把自己显示到大屏
            if (callAction == CallAction.CALL) {
                // 短暂的白屏是 videoCallSurfaceBig 的背景色 #FFF 造成的
                surfaceBig.setVisibility(View.VISIBLE);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addSink(surfaceBig);
            }
            // 默认在没有初始化摄像头时 SurfaceViewRenderer 劈头盖脸给你一个大黑屏
            // 因此使用白色背景代替黑色背景，但是设置了 background 属性后视频就被覆盖了
            // 所以等摄像头初始化好之后才展示视频，去除背景即可显示视频
            new android.os.Handler().postDelayed(() -> {
                surfaceBig.setBackgroundResource(0);
            }, 500);

            // 小窗口
            surfaceSmall = activity.findViewById(R.id.video_call_surface_small);
            surfaceSmall.init(eglBaseContext, null);
            surfaceSmall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            surfaceSmall.setEnableHardwareScaler(false);
            surfaceSmall.setKeepScreenOn(true);
            surfaceSmall.setZOrderMediaOverlay(true); // 位于视窗最上层
        }

        // 设置使用扬声器还是听筒
        AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (callType == CallType.Video) {
            am.setSpeakerphoneOn(true);
        } else {
            am.setSpeakerphoneOn(false);
        }

    }

    // 对视频流的一些配置
    protected void consultConfig(MediaConstraints mediaConstraints) {
        //mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        //mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        //mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "false"));
    }

    // 获取本地视频采集方案
    // 在 Android 系统下有两种 Camera：一种称为 Camera1，是一种比较老的采集视频数据的方式；另一种称为 Camera2，是一种新的采集视频方法。
    // 它们之间的最大区别是 Camera1 使用同步方式调用 API，而 Camera2 使用异步方式调用 API，所以 Camera2 比 Camera1 更高效。
    // 默认情况下，应该尽量使用 Camera2 来采集视频数据。但如果有些机型不支持 Camera2，就只能选择使用 Camera1 了
    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(activity)) {
            return createCameraCapturer(new Camera2Enumerator(activity));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    /**
     * 枚举摄像头
     *
     * @param enumerator
     * @return
     */
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // 首先，尝试获取前置摄像头。通过 isFrontFacing() 判断是否前置摄像头
        // LogKit.p("Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // 如果没有前置摄像头，再尝试获取其他摄像头
        LogKit.p("Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }


    /**
     * 创建安卓 webrtc 工厂类
     *
     * @return
     */
    private PeerConnectionFactory createPeerConnectionFactory() {
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        // 初始化编码与解码工厂，关闭 vp8，使用 h264
        encoderFactory = new DefaultVideoEncoderFactory(
                eglBaseContext, false /* enableIntelVp8Encoder */, true);
        decoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(activity)
                //.setEnableInternalTracer(true) // 监控用
                .createInitializationOptions());

        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);

        return builder.createPeerConnectionFactory();
    }

    /**
     * 创建连接，发送自己的 Candidate 给对端
     * PeerConnection 对象是 WebRTC 最核心的对象，后面音视频数据的传输都靠它来完成
     *
     * @return
     */
    protected PeerConnection createPeerConnection() {
        // 配置 stun / turn 服务器
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        PeerConnection.IceServer stun = PeerConnection.IceServer.builder(Config.STUN)
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer();
        PeerConnection.IceServer turn = PeerConnection.IceServer.builder(Config.TURN)
                .setUsername(Config.TURN_USER)
                .setPassword(Config.TURN_PASS)
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer();
        iceServers.add(stun);
        iceServers.add(turn);
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, peerConnectionObserver);
        peerConnection.addTrack(localAudioTrack);
        if (callType == CallType.Video) {
            peerConnection.addTrack(localVideoTrack);
        }
        return peerConnection;
    }


    /**
     * PeerConnection 的观察者，监测远端发送来的流数据
     */
    private PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            LogKit.p("onSignalingChange " + newState);
        }

        // 如果连接状态不是已连接的话要检查防火墙、NAT、TURN
        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
            LogKit.p("onIceConnectionChange " + newState);
            if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                LogKit.p("ICE连接成功" + newState);
            } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                LogKit.p("断开ICE连接" + newState);
            } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                LogKit.p("ICE连接失败");
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            LogKit.p("onIceConnectionReceivingChange " + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            LogKit.p("onIceGatheringChange " + newState);
            if (newState == PeerConnection.IceGatheringState.COMPLETE) {
                // trickle complete
                LogKit.p("trickle 交换完成");
                if (callType == CallType.Video) {
                    videoCapturer.startCapture(1280, 720, fps);
                }
            }
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {
            // 发送 IceCandidate 到对端
            try {
                com.alibaba.fastjson.JSONObject msgObj = new com.alibaba.fastjson.JSONObject();
                msgObj.put("event", "trickle");
                com.alibaba.fastjson.JSONObject candidateObj = new com.alibaba.fastjson.JSONObject();
                candidateObj.put("sdpMid", candidate.sdpMid);
                candidateObj.put("sdpMLineIndex", candidate.sdpMLineIndex);
                candidateObj.put("sdp", candidate.sdp);
                msgObj.put("candidate", candidateObj);
                socketIO.send(WebrtcSocketIoEvent.message, signallingRoomId, msgObj.toString());
                LogKit.p("发送 IceCandidate 到对端:" + msgObj.toString());
            } catch (com.alibaba.fastjson.JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {
            LogKit.p("onIceCandidatesRemoved");
            peerConnection.removeIceCandidates(candidates);
        }


        @Override
        public void onAddStream(MediaStream stream) {
            // 接收远程远程视频流
            activity.runOnUiThread(() -> {
                LogKit.p("通话已接通");
                CallActivity ca = (CallActivity) activity;
                ca.stopTimer();
                if (callType == CallType.Video) {
                    activity.findViewById(R.id.webrtc_call_avatar).setVisibility(View.GONE);
                }
                activity.findViewById(R.id.webrtc_call_tips).setVisibility(View.GONE);

                if (stream.videoTracks.size() > 0) {
                    surfaceSmall.setVisibility(surfaceBig.VISIBLE);
                    VideoTrack remoteVideoTrack = stream.videoTracks.get(0);
                    remoteVideoTrack.setEnabled(true);
                    // 拨打方切换自己到小屏，对方到大屏
                    if (callAction == CallAction.CALL) {
                        localVideoTrack.removeSink(surfaceBig);
                        localVideoTrack.addSink(surfaceSmall);
                        remoteVideoTrack.addSink(surfaceBig);
                    } else {
                        localVideoTrack.setEnabled(true);
                        localVideoTrack.addSink(surfaceSmall);
                        remoteVideoTrack.addSink(surfaceBig);
                    }
                    //LogKit.p("获取到了远程视频流" + stream.videoTracks.size());
                }
            });
        }

        @Override
        public void onRemoveStream(MediaStream stream) {
            LogKit.p("onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
            LogKit.p("onAddTrack ");
        }
    };


    /**
     * 主叫端创建 SDP 信息并发送 offer 给对端
     */
    public void createOffer() {
        if (peerConnection == null) {
            peerConnection = createPeerConnection();
        }
        // 协商配置
        MediaConstraints mediaConstraints = new MediaConstraints();
        consultConfig(mediaConstraints);
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                LogKit.p("createOffer onCreateSuccess " + sdp.toString());
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        LogKit.p("createOffer setLocalDescription onCreateSuccess");
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSetSuccess() {
                        LogKit.p("createOffer setLocalDescription onSetSuccess");
                        // 发送 offer 到对端
                        try {
                            JSONObject msgObj = new JSONObject();
                            msgObj.put("event", "sdp");
                            msgObj.put("type", sdp.type.toString());
                            msgObj.put("description", sdp.description);
                            socketIO.send(WebrtcSocketIoEvent.message, signallingRoomId, msgObj.toString());
                            LogKit.p("send offer" + msgObj.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCreateFailure(String error) {
                        LogKit.p("createOffer setLocalDescription onCreateFailure " + error);
                    }

                    @Override
                    public void onSetFailure(String error) {
                        LogKit.p("createOffer setLocalDescription onSetFailure " + error);
                    }
                }, sdp);
            }

            @Override
            public void onSetSuccess() {
                LogKit.p("createOffer onSetSuccess");
            }

            @Override
            public void onCreateFailure(String error) {
                LogKit.p("createOffer onCreateFailure " + error);
            }

            @Override
            public void onSetFailure(String error) {
                LogKit.p("createOffer onSetFailure " + error);
            }
        }, mediaConstraints);
    }

    // 被呼叫者回答 offer
    public void createAnswer() {
        if (peerConnection == null) {
            peerConnection = createPeerConnection();
        }
        // 协商配置
        MediaConstraints mediaConstraints = new MediaConstraints();
        consultConfig(mediaConstraints);

        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                    }

                    @Override
                    public void onSetSuccess() {
                        // send answer sdp
                        LogKit.p("createAnswer setLocalDescription onSetSuccess");
                        try {
                            JSONObject msgObj = new JSONObject();
                            msgObj.put("event", "sdp");
                            msgObj.put("type", sdp.type.toString());
                            msgObj.put("description", sdp.description);
                            socketIO.send(WebrtcSocketIoEvent.message, signallingRoomId, msgObj.toString());
                            LogKit.p("send answerOffer:" + msgObj.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        LogKit.p("createAnswer setLocalDescription onCreateFailure " + s);
                    }

                    @Override
                    public void onSetFailure(String s) {
                        LogKit.p("createAnswer setLocalDescription onSetFailure " + s);
                    }
                }, sdp);
            }

            @Override
            public void onSetSuccess() {
                LogKit.p("createAnswer onSetSuccess");
            }

            @Override
            public void onCreateFailure(String s) {
                LogKit.p("createAnswer onCreateFailure " + s);
            }

            @Override
            public void onSetFailure(String s) {
                LogKit.p("createAnswer onSetFailure " + s);
            }
        }, mediaConstraints);
    }


    // 信令服务器长连接消息处理
    private SocketIO.Callback socketCallback = new SocketIO.Callback() {

        @Override
        public void onConnect() {
            socketIO.join(WebrtcSocketIoEvent.join, signallingRoomId);
            if (callAction == CallAction.CALL) {
                // 本地初始化成功并且进入房间成功后再拨号，通知对方进入房间，如果对方在线由对方发起 createOffer
                inviteCall();
                LogKit.p("已连接信令服务器，通知对方拉起来电界面");
            } else if (callAction == CallAction.ANSWER) {
                createOffer();
            }
        }

        @Override
        public void onMessage(String text) {
            try {
                JSONObject msgObj = new JSONObject(text);
                String event = msgObj.getString("event");
                if ("sdp".equals(event)) {
                    String sdpType = msgObj.getString("type");
                    String sdpDescription = msgObj.getString("description");
                    if (sdpType.toLowerCase().equals("offer")) {
                        // 收到 offer
                        if (peerConnection == null) {
                            peerConnection = createPeerConnection();
                        }
                        SessionDescription offerSdp = new SessionDescription(SessionDescription.Type.OFFER, sdpDescription);
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                LogKit.p("receive Offer setRemoteDescription onCreateSuccess");
                            }

                            @Override
                            public void onSetSuccess() {
                                LogKit.p("receive Offer setRemoteDescription onSetSuccess");
                                // 发送 answer
                                createAnswer();
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                LogKit.p("receive Offer setRemoteDescription onCreateFailure " + s);
                            }

                            @Override
                            public void onSetFailure(String s) {
                                LogKit.p("receive Offer setRemoteDescription onSetFailure " + s);
                            }
                        }, offerSdp);
                    } else if (sdpType.toLowerCase().equals("answer")) {
                        // 收到 answer
                        SessionDescription answerSdp = new SessionDescription(SessionDescription.Type.ANSWER, sdpDescription);
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                LogKit.p("receive Answer setRemoteDescription onCreateSuccess");
                            }

                            @Override
                            public void onSetSuccess() {
                                LogKit.p("receive Answer setRemoteDescription onSetSuccess");
                            }

                            @Override
                            public void onCreateFailure(String s) {
                                LogKit.p("receive Answer setRemoteDescription onCreateFailure " + s);
                            }

                            @Override
                            public void onSetFailure(String s) {
                                LogKit.p("receive Answer setRemoteDescription onSetFailure " + s);
                            }
                        }, answerSdp);
                    }
                } else if ("trickle".equals(event)) {
                    // 收到 ICE trickle 信息
                    JSONObject candidateObj = msgObj.getJSONObject("candidate");
                    String sdpMid = candidateObj.getString("sdpMid");
                    int sdpMLineIndex = candidateObj.getInt("sdpMLineIndex");
                    String sdp = candidateObj.getString("sdp");
                    IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                    peerConnection.addIceCandidate(candidate);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 退出 activity 需要销毁资源
     */
    public void destroy() {
        try {
            LogKit.p("webrtc destroy 0");
            if (peerConnection != null) {
                peerConnection.close();
            }
            LogKit.p("webrtc destroy 1");
            if (peerConnectionFactory != null) peerConnectionFactory.dispose();
            LogKit.p("webrtc destroy 2");
            if (videoSource != null) videoSource.dispose();
            LogKit.p("webrtc destroy 3");
            if (videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
            }
            LogKit.p("webrtc destroy 4");
            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.stopListening();
                surfaceTextureHelper.dispose();
            }
            LogKit.p("webrtc destroy 5");
            if (surfaceBig != null) surfaceBig.release();
            if (surfaceSmall != null) surfaceSmall.release();
            LogKit.p("webrtc destroy 6");
            if (socketIO != null) {
                socketIO.destroy();
            }
        } catch (Exception e) {
            LogKit.p("[wbrtc one2one destroy Exception]", e);
        }
    }


    // 邀请对方进行通话
    private void inviteCall() {
        SendPrivateMessageReqDto dto = new SendPrivateMessageReqDto();
        dto.Type = MessageType.ApplyOne2OneVideoCall;
        dto.Message = String.valueOf(callType);
        dto.ToUsers = bizId;
        ImSendMessageHelper.sendPrivateMessage(dto, null);
    }

}
