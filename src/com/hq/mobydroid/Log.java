package com.hq.mobydroid;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger.
 *
 * @author bilux
 */
public class Log {

    private static final Logger LOGGER = Logger.getLogger(MobyDroid.class.getName());
    private static final int LOG_LIMIT = 1024 * 1024;
    private static final int LOG_COUNT = 1;
    private static final boolean LOG_APPEND = true;
    private static boolean logEnabled = false;

    public static void init() {
        try {
            // Create an appending file handler
            // Create a file handler that write log record to a file
            // pattern - the pattern for naming the output file
            // limit - the maximum number of bytes to write to any one file
            // count - the number of files to use
            // append - specifies append mode
            FileHandler fileHandler = new FileHandler(MobydroidStatic.LOG_PATH, LOG_LIMIT, LOG_COUNT, LOG_APPEND);
            // create a custom Formatter
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    StringBuilder builder = new StringBuilder();
                    builder.append((new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")).format(new Date(record.getMillis()))).append(" - ");
                    builder.append("[").append(record.getLevel()).append("] - ");
                    builder.append(formatMessage(record));
                    builder.append("\n");
                    return builder.toString();
                }
            });
            LOGGER.addHandler(fileHandler);
            // enable logging
            logEnabled = true;
            // first test ;)
            Log.log(Level.INFO, "Logger", "initialized");
        } catch (SecurityException | IOException ex) {
            Log.log(Level.SEVERE, "Logger", ex);
        }
    }

    /**
     * Enable logging
     */
    public static void enable() {
        logEnabled = true;
    }

    /**
     * Disable logging. Good for production release.
     */
    public static void disable() {
        logEnabled = false;
    }

    /**
     * write Log message
     *
     * @param level Log level
     * @param key Log message start
     * @param msg Log message
     */
    public static void log(Level level, String key, Object msg) {
        if (logEnabled) {
            LOGGER.log(level, "[{0}] : {1}.", new Object[]{key, msg});
        }
    }
}
