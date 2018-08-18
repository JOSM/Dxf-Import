// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.dxfimport;

import org.openstreetmap.josm.spi.preferences.Config;

/**
 * DXF import settings.
 */
public final class Settings {

    private Settings() {
        // Hide default constructor for utilities classes
    }
    
    public static void setScaleNumerator(double value) {
        Config.getPref().putDouble("importdxf_scalenum", value);
    }
    
    public static void setScaleDivisor(double value) {
        if (value == 0)
            throw new IllegalArgumentException("Scale divisor cannot be 0");
        Config.getPref().putDouble("importdxf_scalediv", value);
    }
    
    public static void setCurveSteps(long value) {
        if (value < 1)
            throw new IllegalArgumentException("Curve steps cannot less than 1");
        Config.getPref().putLong("importdxf_curvesteps", value);
    }
    
    public static double getScaleNumerator() {
        return Config.getPref().getDouble("importdxf_scalenum", 1);
    }
    
    public static double getScaleDivisor() {
        return Config.getPref().getDouble("importdxf_scalediv", 1);
    }
    
    public static double getCurveSteps() {
        return Config.getPref().getDouble("importdxf_curvesteps", 15);
    }
}
