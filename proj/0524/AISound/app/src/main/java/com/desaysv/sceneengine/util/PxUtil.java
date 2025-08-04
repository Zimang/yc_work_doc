package com.desaysv.sceneengine.util;

public class PxUtil {
    public static float DensityDpi;
    public static float ScaledDensity;

    public static int pxTodp(int px) {
        return (int) (px / (DensityDpi / 160));
    }

    public static int pxToSp(int px) {
        return (int) (px / ScaledDensity);
    }

    public static float dpTOpx(float dp) {
        return (DensityDpi / 160) * dp;
    }
}
