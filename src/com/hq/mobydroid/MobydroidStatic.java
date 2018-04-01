package com.hq.mobydroid;

import java.io.File;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class MobydroidStatic {

    public static final String HOME_PATH = System.getProperty("user.home") + File.separator;
    public static final String MOBY_DATA_PATH = HOME_PATH + ".mobydroid" + File.separator;
    public static final String MOBY_HOME_PATH = HOME_PATH + "MobyDroid" + File.separator;
    public static final String SETTINGS_PATH = MOBY_DATA_PATH + "Settings.xml";
    public static final String LOG_PATH = MOBY_DATA_PATH + "MobyDroid.log";
    public static final String MOBY_DEVICE_TEMP_PATH = "/data/local/tmp/modydroid/";
    //public static final String MOBY_JAR_PATH = new File(MobyDroid.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    public static final String MOBY_JAR_PATH = "/home/hq/Desktop/tmp/aapt";
}
