package com.example.call_quality;

import android.content.Context;
import android.view.View;

import com.example.agora_manager.AgoraManagerAuthentication;
import com.example.agora_manager.AgoraManager;

import io.agora.rtc2.Constants;
import io.agora.rtc2.EchoTestConfiguration;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.internal.LastmileProbeConfig;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class AgoraManagerCallQuality extends AgoraManagerAuthentication {
    // Counters to control the frequency of messages
    private int counter = 0;
    // Quality of the remote video stream being played
    private boolean highQuality = true;
    private final IRtcEngineEventHandler baseEventHandler;

    public AgoraManagerCallQuality(Context context) {
        super(context);
        baseEventHandler = super.getIRtcEngineEventHandler();
    }

    public void startProbeTest() {
        if (agoraEngine == null) setupAgoraEngine();

        // Configure a LastmileProbeConfig instance.
        LastmileProbeConfig config = new LastmileProbeConfig();
        // Probe the uplink network quality.
        config.probeUplink = true;
        // Probe the downlink network quality.
        config.probeDownlink = true;
        // The expected uplink bitrate (bps). The value range is [100000,5000000].
        config.expectedUplinkBitrate = 100000;
        // The expected downlink bitrate (bps). The value range is [100000,5000000].
        config.expectedDownlinkBitrate = 100000;
        agoraEngine.startLastmileProbeTest(config);
        sendMessage("Running the last mile probe test ...");
    }

    @Override
    protected boolean setupAgoraEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext;
            config.mAppId = appId;
            config.mEventHandler = getIRtcEngineEventHandler();
            // Configure the log file
            RtcEngineConfig.LogConfig logConfig = new RtcEngineConfig.LogConfig();
            logConfig.fileSizeInKB = 256; // Range 128-1024 Kb
            logConfig.level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_WARN);
            config.mLogConfig = logConfig;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            sendMessage(e.toString());
            return false;
        }

        // Enable the dual stream mode
        agoraEngine.setDualStreamMode(Constants.SimulcastStreamMode.ENABLE_SIMULCAST_STREAM);
        // If you se the mode to AUTO_SIMULCAST_STREAM: the low-quality video
        // steam is not sent; the SDK automatically switches to low-quality after
        // // it receives a request to subscribe to a low-quality video stream.

        // Set audio profile and audio scenario.
        agoraEngine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING);

        // Set the video profile
        VideoEncoderConfiguration videoConfig = new VideoEncoderConfiguration();
        // Set mirror mode
        videoConfig.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_AUTO;
        // Set frameRate
        videoConfig.frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10.getValue();
        // Set bitrate
        videoConfig.bitrate = VideoEncoderConfiguration.STANDARD_BITRATE;
        // Set dimensions
        videoConfig.dimensions =  VideoEncoderConfiguration.VD_640x360;
        // Set orientation mode
        videoConfig.orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
        // Set degradation preference
        videoConfig.degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED;
        // Set compression preference: low latency or quality
        videoConfig.advanceOptions.compressionPreference = VideoEncoderConfiguration.COMPRESSION_PREFERENCE.PREFER_LOW_LATENCY;
        // Apply the configuration
        agoraEngine.setVideoEncoderConfiguration(videoConfig);

        return true;
    }

    @Override
    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {

        return new IRtcEngineEventHandler() {
            @Override
            public void onConnectionStateChanged(int state, int reason) {
                sendMessage("Connection state changed"
                        + "\n New state: " + state
                        + "\n Reason: " + reason);
            }

            @Override
            public void onLastmileQuality(int quality) {
                ((AgoraManagerCallQualityListener) mListener).onLastMileQuality(quality);
            }

            @Override
            public void onLastmileProbeResult(LastmileProbeResult result) {
                agoraEngine.stopLastmileProbeTest();
                // The result object contains the detailed test results that help you
                // manage call quality, for example, the downlink jitter.
                sendMessage("Downlink jitter: " + result.downlinkReport.jitter);
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                ((AgoraManagerCallQualityListener) mListener).onNetworkQuality(
                        uid, txQuality, rxQuality
                );
            }

            @Override
            public void onRtcStats(RtcStats rtcStats) {
                counter += 1;
                String msg = "";

                if (counter == 5)
                    msg = rtcStats.users + " user(s)";
                else if (counter == 10 ) {
                    msg = "Packet loss rate: " + rtcStats.rxPacketLossRate;
                    counter = 0;
                }

                if (msg.length()>0) sendMessage(msg);
            }

            @Override
            public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
                String msg = "Remote video state changed: \n Uid =" + uid
                        + " \n NewState =" + state
                        + " \n reason =" + reason
                        + " \n elapsed =" + elapsed;

                sendMessage(msg);
            }

            @Override
            public void onRemoteVideoStats(RemoteVideoStats stats) {
                ((AgoraManagerCallQualityListener) mListener).onRemoteVideoStats(
                        stats
                );
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                baseEventHandler.onUserJoined(uid, elapsed);
                ((AgoraManagerCallQualityListener) mListener).onUserJoined(
                        uid
                );
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                baseEventHandler.onJoinChannelSuccess(channel, uid, elapsed);
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                baseEventHandler.onUserOffline(uid, reason);
            }

            @Override
            public void onTokenPrivilegeWillExpire(String token) {
                baseEventHandler.onTokenPrivilegeWillExpire(token);
            }
        };
    }

    public void startEchoTest() {
        if (agoraEngine == null) setupAgoraEngine();
        // Set test configuration parameters
        EchoTestConfiguration echoConfig = new EchoTestConfiguration();
        echoConfig.enableAudio = true;
        echoConfig.enableVideo = true;
        echoConfig.channelId = channelName;
        echoConfig.intervalInSeconds = 2;
        // Set up the video view
        setupLocalVideo();
        echoConfig.view = localSurfaceView;

        // Get a token from the server or from the config file
        if (serverUrl.contains("http")) { // A valid server url is available
            // Fetch a token from the server for channelName
            // Uses the uid from the config.json file
            fetchToken(channelName, 0, new AgoraManagerAuthentication.TokenCallback() {
                @Override
                public void onTokenReceived(String rtcToken) {
                    // Handle the received rtcToken
                    echoConfig.token = rtcToken ;
                    // Start the echo test
                    agoraEngine.startEchoTest(echoConfig);
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle the error
                    sendMessage("Error: " + errorMessage);
                }
            });
        } else { // use the token from the config.json file
            echoConfig.token = config.optString("rtcToken");
            // Start the echo test
            agoraEngine.startEchoTest(echoConfig);
        }
    }

    public void stopEchoTest() {
        agoraEngine.stopEchoTest();
        activity.runOnUiThread(() -> localSurfaceView.setVisibility(View.GONE));
        destroyAgoraEngine();
    }

    public void switchStreamQuality() {
        if (!isJoined() || remoteUid <=0) return;
        highQuality = !highQuality;

        if (highQuality) {
            agoraEngine.setRemoteVideoStreamType(remoteUid, Constants.VIDEO_STREAM_HIGH);
            sendMessage("Switching to high-quality video");
        } else {
            agoraEngine.setRemoteVideoStreamType(remoteUid, Constants.VIDEO_STREAM_LOW);
            sendMessage("Switching to low-quality video");
        }
    }

    public interface AgoraManagerCallQualityListener extends AgoraManager.AgoraManagerListener {
        void onMessageReceived(String message);
        void onNetworkQuality(int uid, int txQuality, int rxQuality);
        void onLastMileQuality(int quality);
        void onUserJoined(int uid);
        void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats);
    }

}
