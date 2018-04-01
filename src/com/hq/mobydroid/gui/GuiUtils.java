package com.hq.mobydroid.gui;

import com.hq.materialdesign.MaterialColor;
import com.hq.mobydroid.device.TaskWorker;
import java.awt.Color;
import javax.swing.Icon;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class GuiUtils {

    // APK PACKAGE ICON SIZE
    public static final int APK_ICON_WIDTH = 32;
    public static final int APK_ICON_HEIGTH = 32;

    /**
     * get formated size text from file size
     *
     * @param value the size in byte
     * @return the formated size text
     */
    public static String getFormatedSize(long value) {
        String sizeStr;
        if (value >= (1024 * 1024 * 1024)) {
            sizeStr = String.format("%.2f", ((double) value / (1024 * 1024 * 1024))) + " GB";
        } else if (value >= (1024 * 1024)) {
            sizeStr = String.format("%.2f", ((double) value / (1024 * 1024))) + " MB";
        } else if (value >= 1024) {
            sizeStr = String.format("%.2f", ((double) value / (1024))) + " kB";
        } else {
            sizeStr = String.valueOf(value) + " Byte";
        }
        return (sizeStr);
    }

    /**
     * Get The Hex Value From Color
     *
     * @param status the color
     * @return the status color
     */
    public static Color getStatusColor(TaskWorker.Status status) {
        switch (status) {
            case PENDING:
                return MaterialColor.DEEPPURPLE_700;
            case STARTED:
                return MaterialColor.BLUE_700;
            case DONE:
                return MaterialColor.GREEN_700;
            case CANCELLED:
                return MaterialColor.GREY_700;
            case FAILED:
                return MaterialColor.RED_700;
            default:
                return MaterialColor.GREY_700;
        }
    }

    /**
     * Get The Hex Value From Color
     *
     * @param color the color
     * @return the hex text
     */
    public static String getHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     *
     * @param numStr
     * @return
     */
    public static long strToLong(String numStr) {
        try {
            int numLen = 0;
            while ((numLen < numStr.length()) && (Character.isDigit(numStr.charAt(numLen)))) {
                numLen++;
            }
            return (Long.parseLong(numStr.substring(0, numLen)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     *
     * @param string
     * @return
     */
    public static String removeQuote(String string) {
        if (string.startsWith("'") || string.startsWith("\"")) {
            string = string.substring(1, string.length());
        }
        if (string.endsWith("'") || string.endsWith("\"")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     *
     * @param string
     * @return
     */
    public static String quote(String string) {
        // Check that string contains no whitespace
        if (string.matches("\\S+")) {
            return string;
        }
        return "'" + string.replace("'", "'\\''") + "'";
    }

    /**
     * Get file icon from file type.
     * 
     * @param fileType
     * @return
     */
    public static Icon getIcon(String fileType) {
        switch (fileType) {
            case "avi":
                return ResourceLoader.avi24Icon;
            case "css":
                return ResourceLoader.css24Icon;
            case "xl":
            case "xlx":
            case "xlsx":
                return ResourceLoader.excel24Icon;
            case "htm":
            case "html":
            case "js":
            case "xml":
                return ResourceLoader.html24Icon;
            case "jpg":
            case "jpeg":
            case "bmp":
            case "tif":
            case "gif":
            case "webp":
                return ResourceLoader.jpg24Icon;
            case "mp3":
            case "aa3":
            case "aac":
            case "amr":
            case "aif":
            case "mid":
            case "midi":
            case "mpa":
            case "ogg":
            case "pls":
            case "ra":
            case "ram":
            case "wav":
            case "wma":
                return ResourceLoader.mp324Icon;
            case "mp4":
            case "3gp":
            case "dvx":
            case "flv":
            case "mkv":
            case "mov":
            case "mpeg":
            case "mpg":
            case "rm":
            case "rmv":
            case "ts":
            case "webm":
            case "wmv":
                return ResourceLoader.mp424Icon;
            case "pdf":
                return ResourceLoader.pdf24Icon;
            case "png":
                return ResourceLoader.png24Icon;
            case "ppt":
            case "pptx":
                return ResourceLoader.ppt24Icon;
            case "doc":
            case "docx":
                return ResourceLoader.word24Icon;
            case "zip":
            case "tar":
            case "gz":
            case "bz":
            case "rar":
            case "7z":
                return ResourceLoader.zip24Icon;
            default:
                return ResourceLoader.file24Icon;
        }
    }
}
