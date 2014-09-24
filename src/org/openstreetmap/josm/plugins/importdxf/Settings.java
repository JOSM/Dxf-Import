package org.openstreetmap.josm.plugins.importdxf;

import org.openstreetmap.josm.Main;

public class Settings {

    public static void setScaleNumerator(double value) {
        Main.pref.putDouble("importdxf_scalenum", value);
    }
    public static void setScaleDivisor(double value) {
        if (value == 0)
            throw new IllegalArgumentException("Scale divisor cannot be 0");
        Main.pref.putDouble("importdxf_scalediv", value);
    }
    public static void setCurveSteps(long value) {
        if (value < 1)
            throw new IllegalArgumentException("Curve steps cannot less than 1");
        Main.pref.putLong("importdxf_curvesteps", value);
    }
    
    public static double getScaleNumerator() {
        return Main.pref.getDouble("importdxf_scalenum", 1);
    }
    public static double getScaleDivisor() {
        return Main.pref.getDouble("importdxf_scalediv", 1);
    }
    public static double getCurveSteps() {
        return Main.pref.getDouble("importdxf_curvesteps", 15);
    }
}
