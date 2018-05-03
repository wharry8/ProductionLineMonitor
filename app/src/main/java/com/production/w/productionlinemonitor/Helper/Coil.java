package com.production.w.productionlinemonitor.Helper;

/**
 * Created by w on 4/26/2018.
 */

public class Coil {
    public static final int systemRunning = 7;
    public static final int systemStopped = 8;
    public static final int systemError = 9;

    public static final int station1Running = 3100;
    public static final int station1Stopped = 3101;
    public static final int station1Error = 3102;

    public static final int station2Running = 3105;
    public static final int station2Stopped = 3106;
    public static final int station2Error = 3107;

    public static final int station3Running = 3110;
    public static final int station3Stopped = 3111;
    public static final int station3Error = 3112;

    public static final int station4Running = 3115;
    public static final int station4Stopped = 3116;
    public static final int station4Error = 3117;

    public static final int station5Running = 3120;
    public static final int station5Stopped = 3121;
    public static final int station5Error = 3122;

    public static final int outputFull = 400;

    public static final int transmissionLineRunning = 3130;
    public static final int transmissionLineStopped = 3136;

    // 分界线

    public static final int car1AtStartPosition = 6100;
    public static final int car1AtStartBlockPosition = 6101;

    public static final int car1AtStation1StoragePosition = 6102;
    public static final int car1AtStation1ProcessingPosition = 6103;
    public static final int car1AtStation1CompletionPosition = 6104;

    public static final int car1AtStation2StoragePosition = 6105;
    public static final int car1AtStation2ProcessingPosition = 6106;
    public static final int car1AtStation2CompletionPosition = 6107;

    public static final int car1AtStation3StoragePosition = 6108;
    public static final int car1AtStation3ProcessingPosition = 6109;

    public static final int car2AtStation3ProcessingPosition = 6110;
    public static final int car2AtStation3CompletionPosition = 6111;

    public static final int car2AtStation4StoragePosition = 6112;
    public static final int car2AtStation4ProcessingPosition = 6113;
    public static final int car2AtStation4CompletionPosition = 6114;

    public static final int car2AtStation5StoragePosition = 6115;
    public static final int car2AtStation5ProcessingPosition = 6116;

    public static final int car2AtEndArea = 6117;
    public static final int car2AtEndPosition = 6118;

    public static final int car1HookOut = 593;
    public static final int car1HookIn = 592;

    public static final int car2HookOut = 714;
    public static final int car2HookIn = 713;


    public static final int station1ProcessingPositionBlocked = 5201;
    public static final int station1ProcessingPositionNotBlocked = 5203;

    public static final int station1ProcessingPositionUp = 5205;
    public static final int station1ProcessingPositionDown = 5207;

    public static final int station1StoragePositionBlocked = 5211;
    public static final int station1StoragePositionNotBlocked = 5213;

    public static final int station1StoragePositionUp = 5215;
    public static final int station1StoragePositionDown = 5217;

    public static final int station2ProcessingPositionBlocked = 5221;
    public static final int station2ProcessingPositionNotBlocked = 5223;

    public static final int station2ProcessingPositionUp = 5225;
    public static final int station2ProcessingPositionDown = 5227;

    public static final int station2StoragePositionBlocked = 5231;
    public static final int station2StoragePositionNotBlocked = 5233;

    public static final int station2StoragePositionUp = 5235;
    public static final int station2StoragePositionDown = 5237;

    public static final int station3ProcessingPositionBlocked = 5241;
    public static final int station3ProcessingPositionNotBlocked = 5243;

    public static final int station3ProcessingPositionUp = 5245;
    public static final int station3ProcessingPositionDown = 5247;

    public static final int station3StoragePositionBlocked = 5251;
    public static final int station3StoragePositionNotBlocked = 5253;

    public static final int station3StoragePositionUp = 5255;
    public static final int station3StoragePositionDown = 5257;


    public static final int station4ProcessingPositionBlocked = 5261;
    public static final int station4ProcessingPositionNotBlocked = 5263;
    public static final int station4ProcessingPositionUp = 5265;
    public static final int station4ProcessingPositionDown = 5267;

    public static final int station4StoragePositionBlocked = 5271;
    public static final int station4StoragePositionNotBlocked = 5273;
    public static final int station4StoragePositionUp = 5275;
    public static final int station4StoragePositionDown = 5277;

    public static final int station5ProcessingPositionBlocked = 5281;
    public static final int station5ProcessingPositionNotBlocked = 5283;
    public static final int station5ProcessingPositionUp = 5285;
    public static final int station5ProcessingPositionDown = 5287;

    public static final int station5StoragePositionBlocked = 5291;
    public static final int station5StoragePositionNotBlocked = 5293;
    public static final int station5StoragePositionUp = 5295;
    public static final int station5StoragePositionDown = 5297;

    public static final int storagePositionBlocked = 5301;
    public static final int storagePositionNotBlocked = 5303;

    // 分界线
    // 机械手相关信号

    public static final int station1VerticallyToWaitPosition = 7000;
    public static final int station1VerticallyToFetchPosition = 7001;
    public static final int station1VerticallyToLeftPutPosition = 7002;
    public static final int station1VerticallyToRightPutPosition = 7003;

    public static final int station1HorizontallyToFetchPosition = 7004;
    public static final int station1HorizontallyToLeftPutPosition = 7005;
    public static final int station1HorizontallyToRightPutPosition = 7006;

    public static final int station2VerticallyToWaitPosition = 8000;
    public static final int station2VerticallyToFetchPosition = 8001;
    public static final int station2VerticallyToLeftPutPosition = 8002;
    public static final int station2VerticallyToRightPutPosition = 8003;

    public static final int station2HorizontallyToFetchPosition = 8004;
    public static final int station2HorizontallyToLeftPutPosition = 8005;
    public static final int station2HorizontallyToRightPutPosition = 8006;

    public static final int station3VerticallyToWaitPosition = 8040;
    public static final int station3VerticallyToFetchPosition = 8041;
    public static final int station3VerticallyToLeftPutPosition = 8042;
    public static final int station3VerticallyToRightPutPosition = 8043;

    public static final int station3HorizontallyToFetchPosition = 8044;
    public static final int station3HorizontallyToLeftPutPosition = 8045;
    public static final int station3HorizontallyToRightPutPosition = 8046;

    public static final int station4VerticallyToWaitPosition = 8080;
    public static final int station4VerticallyToFetchPosition = 8081;
    public static final int station4VerticallyToLeftPutPosition = 8082;
    public static final int station4VerticallyToRightPutPosition = 8083;

    public static final int station4HorizontallyToFetchPosition = 8084;
    public static final int station4HorizontallyToLeftPutPosition = 8085;
    public static final int station4HorizontallyToRightPutPosition = 8086;

    public static final int station5VerticallyToWaitPosition = 8120;
    public static final int station5VerticallyToFetchPosition = 8121;
    public static final int station5VerticallyToLeftPutPosition = 8122;
    public static final int station5VerticallyToRightPutPosition = 8123;

    public static final int station5HorizontallyToFetchPosition = 8124;
    public static final int station5HorizontallyToLeftPutPosition = 8125;
    public static final int station5HorizontallyToRightPutPosition = 8126;
}

