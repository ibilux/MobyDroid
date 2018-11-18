package com.hq.mobydroid;

/**
 * Utils.
 *
 * @author bilux
 */
public class Utils {

    /**
     * Assert string from null exception
     *
     * @param str
     * @return 
     */
    public static String assertString(final String str) {
        return str == null ? "" : str;
    }

    /**
     * Null-safe, short-circuit evaluation.
     *
     * @param str
     * @return 
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }
}
