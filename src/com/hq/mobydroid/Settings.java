package com.hq.mobydroid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Settings.
 *
 * @author bilux
 */
public class Settings {

    // properties file location
    private static final Properties properties = new Properties();

    // pre-settings
    private static final Map<String, String> settings = new HashMap() {
        {
            put("Expert_Settings", "true");
            put("AppInstaller_Onsdcard", "false");
            put("AppInstaller_Reinstall", "true");
            put("AppInstaller_Downgrade", "true");
            put("AppInstaller_AddPackagePath", MobydroidStatic.MOBY_HOME_PATH);
            put("AppManager_RestorePath", MobydroidStatic.MOBY_HOME_PATH);
            put("ScreenCapture_SavePath", MobydroidStatic.MOBY_HOME_PATH);
            put("Terminal_SavePath", MobydroidStatic.MOBY_HOME_PATH);
        }
    };

    /**
     * Set Setting.
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        settings.put(key, value);
    }

    public static String get(String key) {
        return settings.get(key);
    }

    /**
     * Save Settings.
     */
    public static void save() {
        // add settings values to properties
        properties.putAll(settings);
        try {
            // save to xml
            properties.storeToXML(new FileOutputStream(MobydroidStatic.SETTINGS_PATH), null);
        } catch (FileNotFoundException ex) {
            Log.log(Level.SEVERE, "Save Settings", ex);
        } catch (IOException ex) {
            Log.log(Level.SEVERE, "Save Settings", ex);
        }
    }

    /**
     * Load Settings.
     */
    public static void load() {
        try {
            // load from xml
            properties.loadFromXML(new FileInputStream(MobydroidStatic.SETTINGS_PATH));
            // add non empty values
            properties.entrySet().stream().filter(entry -> (entry.getValue() != null && !entry.getValue().toString().isEmpty())).forEach(entry -> {
                settings.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            });

            /*properties.entrySet().forEach(entry -> {
                settings.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            });*/
        } catch (FileNotFoundException ex) {
            Log.log(Level.SEVERE, "Load Settings", ex);
        } catch (IOException ex) {
            Log.log(Level.SEVERE, "Load Settings", ex);
        }
    }
}
