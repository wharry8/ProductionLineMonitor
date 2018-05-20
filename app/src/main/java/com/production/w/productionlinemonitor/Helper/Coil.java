package com.production.w.productionlinemonitor.Helper;

/**
 * Created by w on 4/26/2018.
 */

public class Coil {
    // todo
    // 根据最新的映射更新信号
    // used by real animation
    // 生产线运行状态
    public static final int systemRunning = 7;
    public static final int systemStopped = 8;
    public static final int systemError = 9;

    // cnc 状态
    public static final int station1LeftCNCWorking = 1;
    public static final int station1RightCNCWorking = 1;

    public static final int station2LeftCNCWorking = 1;
    public static final int station2RightCNCWorking = 2;

    public static final int station3LeftCNCWorking = 1;
    public static final int station3RightCNCWorking = 3;

    public static final int station4LeftCNCWorking = 1;
    public static final int station4RightCNCWorking = 4;

    public static final int station5LeftCNCWorking = 1;
    public static final int station5RightCNCWorking = 5;

    // 工站运行状态
    public static final int station1Running = 10;
    public static final int station1Stopped = 11;
    public static final int station1Error = 12;

    public static final int station2Running = 15;
    public static final int station2Stopped = 16;
    public static final int station2Error = 17;

    public static final int station3Running = 20;
    public static final int station3Stopped = 21;
    public static final int station3Error = 22;

    public static final int station4Running = 25;
    public static final int station4Stopped = 26;
    public static final int station4Error = 27;

    public static final int station5Running = 30;
    public static final int station5Stopped = 31;
    public static final int station5Error = 32;

    public static final int startPositionHasBox = 310;
    public static final int station1StoragePositionHasBox = 311;
    public static final int station1ProcessingPositionHasBox = 312;
    public static final int station1CompletionPositionHasBox = 313;
    public static final int station2StoragePositionHasBox = 314;
    public static final int station2ProcessingPositionHasBox = 315;
    public static final int station2CompletionPositionHasBox = 316;
    public static final int station3StoragePositionHasBox = 317;
    public static final int station3ProcessingPositionHasBox = 318;
    public static final int station3CompletionPositionHasBox = 319;
    public static final int station4StoragePositionHasBox = 320;
    public static final int station4ProcessingPositionHasBox = 321;
    public static final int station4CompletionPositionHasBox = 322;
    public static final int station5StoragePositionHasBox = 323;
    public static final int station5ProcessingPositionHasBox = 324;
    // 工站状态结束

    public static final int outputFull = 400;

    // 输送线状态
    public static final int transmissionLineRunning = 3130;
    public static final int transmissionLineStopped = 3136;
    // 输送线结束

    // 小车位置信号
    // todo
    // 根据最新的表格跟新小车位置信号
    public static final int car1AtStartPosition = 41;
    public static final int car1AtStartBlockPosition = 41;

    public static final int car1AtStation1StoragePosition = 42;
    public static final int car1AtStation1ProcessingPosition = 43;
    public static final int car1AtStation1CompletionPosition = 44;

    public static final int car1AtStation2StoragePosition = 45;
    public static final int car1AtStation2ProcessingPosition = 46;
    public static final int car1AtStation2CompletionPosition = 47;

    public static final int car1AtStation3StoragePosition = 48;
    public static final int car1AtStation3ProcessingPosition = 49;

    public static final int car2AtStation3ProcessingPosition = 50;
    public static final int car2AtStation3CompletionPosition = 51;

    public static final int car2AtStation4StoragePosition = 52;
    public static final int car2AtStation4ProcessingPosition = 53;
    public static final int car2AtStation4CompletionPosition = 54;

    public static final int car2AtStation5StoragePosition = 55;
    public static final int car2AtStation5ProcessingPosition = 56;

    public static final int car2AtEndArea = 57;
    public static final int car2AtEndPosition = 57;
    // 小车位置信号结束

    // 小车出钩,回钩信号
    public static final int car1HookOut = 38;
    public static final int car1HookIn = 37;

    public static final int car2HookOut = 40;
    public static final int car2HookIn = 39;
    // 小车出钩,回钩信号结束

    // 挡块信号
    // todo
    // 更新地址, 目前的地址是假的
    public static final int startPositionBlocked = 201;

    public static final int station1ProcessingPositionBlocked = 101;
    public static final int station1ProcessingPositionNotBlocked = 103;

    public static final int station1ProcessingPositionUp = 105;
    public static final int station1ProcessingPositionDown = 107;

    public static final int station1StoragePositionBlocked = 111;
    public static final int station1StoragePositionNotBlocked = 113;

    public static final int station1StoragePositionUp = 115;
    public static final int station1StoragePositionDown = 117;

    public static final int station2ProcessingPositionBlocked = 121;
    public static final int station2ProcessingPositionNotBlocked = 123;

    public static final int station2ProcessingPositionUp = 125;
    public static final int station2ProcessingPositionDown = 127;

    public static final int station2StoragePositionBlocked = 131;
    public static final int station2StoragePositionNotBlocked = 133;

    public static final int station2StoragePositionUp = 135;
    public static final int station2StoragePositionDown = 137;

    public static final int station3ProcessingPositionBlocked = 141;
    public static final int station3ProcessingPositionNotBlocked = 143;

    public static final int station3ProcessingPositionUp = 145;
    public static final int station3ProcessingPositionDown = 147;

    public static final int station3StoragePositionBlocked = 151;
    public static final int station3StoragePositionNotBlocked = 153;

    public static final int station3StoragePositionUp = 155;
    public static final int station3StoragePositionDown = 157;

    public static final int station4ProcessingPositionBlocked = 161;
    public static final int station4ProcessingPositionNotBlocked = 163;
    public static final int station4ProcessingPositionUp = 165;
    public static final int station4ProcessingPositionDown = 167;

    public static final int station4StoragePositionBlocked = 171;
    public static final int station4StoragePositionNotBlocked = 173;
    public static final int station4StoragePositionUp = 175;
    public static final int station4StoragePositionDown = 177;

    public static final int station5ProcessingPositionBlocked = 181;
    public static final int station5ProcessingPositionNotBlocked = 183;
    public static final int station5ProcessingPositionUp = 185;
    public static final int station5ProcessingPositionDown = 187;

    public static final int station5StoragePositionBlocked = 191;
    public static final int station5StoragePositionNotBlocked = 193;
    public static final int station5StoragePositionUp = 195;
    public static final int station5StoragePositionDown = 197;

    public static final int storagePositionBlocked = 201;
    public static final int storagePositionNotBlocked = 203;
    // 挡块\料盒上下信号结束

    // 分界线
    // 机械手到位信号, 主要用于同步
    public static final int hand1AtMiddleTop = 1;
    public static final int hand1AtMiddleBottom = 1;
    public static final int hand1AtRightTop = 1;
    public static final int hand1AtRightBottom = 1;
    public static final int hand1AtLeftTop = 1;
    public static final int hand1AtLeftBottom = 1;

    public static final int hand1FirstTimeToRight1 = 2952;
    public static final int hand1FirstTimeToRight2 = 2943;
    public static final int hand1ToRight1 = 2952;
    public static final int hand1ToRight2 = 2944;

    public static final int hand1FirstTimeToMiddle1 = 2952;
    public static final int hand1FirstTimeToMiddle2 = 2943;
    public static final int Hand1FirstTimeToMiddle3 = 64;

    public static final int hand1ToMiddle1 = 2952;
    public static final int hand1ToMiddle2 = 2943;
    public static final int hand1ToMiddle3 = 64;

    public static final int hand2AtMiddleTop = 1;
    public static final int hand2AtMiddleBottom = 1;
    public static final int hand2AtRightTop = 1;
    public static final int hand2AtRightBottom = 1;
    public static final int hand2AtLeftTop = 1;
    public static final int hand2AtLeftBottom = 1;

    public static final int hand2FirstTimeToRight1 = 2952;
    public static final int hand2FirstTimeToRight2 = 2943;
    public static final int hand2ToRight1 = 2952;
    public static final int hand2ToRight2 = 2944;

    public static final int hand2FirstTimeToMiddle1 = 2952;
    public static final int hand2FirstTimeToMiddle2 = 2943;
    public static final int Hand2FirstTimeToMiddle3 = 64;

    public static final int hand2ToMiddle1 = 2952;
    public static final int hand2ToMiddle2 = 2943;
    public static final int hand2ToMiddle3 = 64;


    public static final int hand3AtMiddleTop = 1;
    public static final int hand3AtMiddleBottom = 1;
    public static final int hand3AtRightTop = 1;
    public static final int hand3AtRightBottom = 1;
    public static final int hand3AtLeftTop = 1;
    public static final int hand3AtLeftBottom = 1;

    public static final int hand3FirstTimeToRight1 = 2952;
    public static final int hand3FirstTimeToRight2 = 2943;
    public static final int hand3ToRight1 = 2952;
    public static final int hand3ToRight2 = 2944;

    public static final int hand3FirstTimeToMiddle1 = 2952;
    public static final int hand3FirstTimeToMiddle2 = 2943;
    public static final int Hand3FirstTimeToMiddle3 = 64;

    public static final int hand3ToMiddle1 = 2952;
    public static final int hand3ToMiddle2 = 2943;
    public static final int hand3ToMiddle3 = 64;

    public static final int hand4AtMiddleTop = 1;
    public static final int hand4AtMiddleBottom = 1;
    public static final int hand4AtRightTop = 1;
    public static final int hand4AtRightBottom = 1;
    public static final int hand4AtLeftTop = 1;
    public static final int hand4AtLeftBottom = 1;

    public static final int hand4FirstTimeToRight1 = 482;
    public static final int hand4FirstTimeToRight2 = 473;
    public static final int hand4ToRight1 = 482;
    public static final int hand4ToRight2 = 474;

    public static final int hand4FirstTimeToMiddle1 = 482;
    public static final int hand4FirstTimeToMiddle2 = 473;
    public static final int hand4FirstTimeToMiddle3 = 64;

    public static final int hand4ToMiddle1 = 482;
    public static final int hand4ToMiddle2 = 474;
    public static final int hand4ToMiddle3 = 64;


    public static final int hand5AtMiddleTop = 1;
    public static final int hand5AtMiddleBottom = 1;
    public static final int hand5AtRightTop = 1;
    public static final int hand5AtRightBottom = 1;
    public static final int hand5AtLeftTop = 1;
    public static final int hand5AtLeftBottom = 1;

    public static final int hand5FirstTimeToRight1 = 2952;
    public static final int hand5FirstTimeToRight2 = 2943;
    public static final int hand5ToRight1 = 2952;
    public static final int hand5ToRight2 = 2944;

    public static final int hand5FirstTimeToMiddle1 = 2952;
    public static final int hand5FirstTimeToMiddle2 = 2943;
    public static final int Hand5FirstTimeToMiddle3 = 64;

    public static final int hand5ToMiddle1 = 2952;
    public static final int hand5ToMiddle2 = 2943;
    public static final int hand5ToMiddle3 = 64;

    // 机械手到位信号结束
    // 机械手相关信号 (目前无法使用)

    public static final int station1VerticallyToWaitPosition = 210;
    public static final int station1VerticallyToFetchPosition = 211;
    public static final int station1VerticallyToLeftPutPosition = 212;
    public static final int station1VerticallyToRightPutPosition = 213;

    public static final int station1HorizontallyToFetchPosition = 214;
    public static final int station1HorizontallyToLeftPutPosition = 215;
    public static final int station1HorizontallyToRightPutPosition = 216;

    public static final int station2VerticallyToWaitPosition = 230;
    public static final int station2VerticallyToFetchPosition = 231;
    public static final int station2VerticallyToLeftPutPosition = 232;
    public static final int station2VerticallyToRightPutPosition = 233;

    public static final int station2HorizontallyToFetchPosition = 234;
    public static final int station2HorizontallyToLeftPutPosition = 235;
    public static final int station2HorizontallyToRightPutPosition = 236;

    public static final int station3VerticallyToWaitPosition = 250;
    public static final int station3VerticallyToFetchPosition = 251;
    public static final int station3VerticallyToLeftPutPosition = 252;
    public static final int station3VerticallyToRightPutPosition = 253;

    public static final int station3HorizontallyToFetchPosition = 254;
    public static final int station3HorizontallyToLeftPutPosition = 255;
    public static final int station3HorizontallyToRightPutPosition = 256;

    public static final int station4VerticallyToWaitPosition = 270;
    public static final int station4VerticallyToFetchPosition = 271;
    public static final int station4VerticallyToLeftPutPosition = 272;
    public static final int station4VerticallyToRightPutPosition = 273;

    public static final int station4HorizontallyToFetchPosition = 274;
    public static final int station4HorizontallyToLeftPutPosition = 275;
    public static final int station4HorizontallyToRightPutPosition = 276;

    public static final int station5VerticallyToWaitPosition = 290;
    public static final int station5VerticallyToFetchPosition = 291;
    public static final int station5VerticallyToLeftPutPosition = 292;
    public static final int station5VerticallyToRightPutPosition = 293;

    public static final int station5HorizontallyToFetchPosition = 294;
    public static final int station5HorizontallyToLeftPutPosition = 295;
    public static final int station5HorizontallyToRightPutPosition = 296;
    // end

    // used by fake_animation
    // 状态信号
    /*
    public static final int systemRunning = 7;
    public static final int systemStopped = 8;
    public static final int systemError = 9;

    public static final int station1LeftCNCWorking = 1;
    public static final int station1RightCNCWorking = 1;
    public static final int station2LeftCNCWorking = 1;
    public static final int station2RightCNCWorking = 1;
    public static final int station3LeftCNCWorking = 1;
    public static final int station3RightCNCWorking = 1;
    public static final int station4LeftCNCWorking = 1;
    public static final int station4RightCNCWorking = 1;
    public static final int station5LeftCNCWorking = 1;
    public static final int station5RightCNCWorking = 1;

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
    */
}

