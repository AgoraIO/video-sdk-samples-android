package com.example.call_quality;

import android.content.Context;
import android.view.View;

import com.example.authentication_workflow.AgoraManagerAuthenticationWorkflow;

import io.agora.rtc2.Constants;
import io.agora.rtc2.EchoTestConfiguration;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.internal.LastmileProbeConfig;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class AgoraManagerCallQuality extends AgoraManagerAuthenticationWorkflow {
    // Counters to control the frequency of messages
    private int counter1 = 0; 
    private int counter2 = 0; 
    // Quality of the remote video stream being played
    private boolean highQuality = true; 

    public AgoraManagerCallQuality(Context context) {
        super(context);
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
        agoraEngine.enableDualStreamMode(true);
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
            // Listen for the remote host joining the channel to get the uid of the host.
            public void onUserJoined(int uid, int elapsed) {
                sendMessage("Remote user joined " + uid);
                remoteUid = uid;

                // Set the remote video view
                setupRemoteVideo();
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                joined = true;
                sendMessage("Joined Channel " + channel);
                localUid = uid;
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                sendMessage("Remote user offline " + uid + " " + reason);
                activity.runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
            }

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
                counter1 += 1;
                String msg = "";

                if (counter1 == 5)
                    msg = rtcStats.users + " user(s)";
                else if (counter1 == 10 ) {
                    msg = "Packet loss rate: " + rtcStats.rxPacketLossRate;
                    counter1 = 0;
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
                counter2 += 1;

                if (counter2 == 5) {
                    String msg = "Remote Video Stats: "
                            + "\n User id =" + stats.uid
                            + "\n Received bitrate =" + stats.receivedBitrate
                            + "\n Total frozen time =" + stats.totalFrozenTime;
                    counter2 = 0;
                    sendMessage(msg);
                }
            }
        };
    }

    public void startEchoTest(String token) {
        if (agoraEngine == null) setupAgoraEngine();
        EchoTestConfiguration echoConfig = new EchoTestConfiguration();
        echoConfig.enableAudio = true;
        echoConfig.enableVideo = true;
        echoConfig.token = token ;
        echoConfig.channelId = channelName;

        setupLocalVideo();
        echoConfig.view = localSurfaceView;
        localSurfaceView.setVisibility(View.VISIBLE);
        agoraEngine.startEchoTest(echoConfig);
    }

    public void stopEchoTest() {
        agoraEngine.stopEchoTest();
        setupLocalVideo();
        localSurfaceView.setVisibility(View.GONE);
        destroyAgoraEngine();
    }

    public void switchStreamQuality() {
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
    }

}
