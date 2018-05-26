package com.production.w.productionlinemonitor.Helper;

/**
 * Created by w on 4/25/2018.
 */

public class Constants {
    public static final int LEFT = -1;
    public static final int RIGHT = 1;
    public static final int CoilStart = 0;
    public static final int CoilLen = 600;

    public static final int RegisterStart = 100;
    public static final int RegisterLen = 100;

    public static final int handRising = 1;
    public static final int handDeclining = 2;
    public static final int handLeftShifting = 3;
    public static final int handRightShifting = 4;
    public static final int handStatic = 5;
    public static final int handRised = 6;
    public static final int handDeclined = 7;
    public static final int handLeftShifted = 8;
    public static final int handRightShifted = 9;

    public static int previousHand4Position = HandPosition.Middle ;
    public static int previousHand2Position = HandPosition.Middle;

    // hand position
    public static final int HAND_LEFT_TOP = 1;
    public static final int HAND_LEFT_BOTTOM = 2;
    public static final int HAND_MIDDLE_TOP = 3;
    public static final int HAND_MIDDLE_BOTTOM = 4;
    public static final int HAND_RIGHT_TOP = 5;
    public static final int HAND_RIGHT_BOTTOM = 6;

    // hand position end


    public static final int BOX_RISING = 1;
    public static final int BOX_RISED = 2;
    public static final int BOX_DECLING = 3;
    public static final int BOX_DECLINED = 4;

    public static float glWidth;
    public static float glHeight;
    public static float unitWidth;
    public static float unitHeight;

    public static final int SUCCESS = 1;
    public static final int WARNING = 2;
    public static final int DANGER = 3;
    public static final int STOPPED = 4;
}
