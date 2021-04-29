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

    /**
     * NumberFormatException-safe, String to integer parser.
     *
     * @param numStr
     * @return
     */
    public static int strToInt(String numStr) {
        try {
            int numLen = 0;
            while ((numLen < numStr.length()) && (Character.isDigit(numStr.charAt(numLen)))) {
                numLen++;
            }
            return (Integer.parseInt(numStr.substring(0, numLen)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
