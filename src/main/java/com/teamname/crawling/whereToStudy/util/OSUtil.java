package com.teamname.crawling.whereToStudy.util;

public class OSUtil {
    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return "unix";
        } else {
            return "unknown";
        }
    }
}
