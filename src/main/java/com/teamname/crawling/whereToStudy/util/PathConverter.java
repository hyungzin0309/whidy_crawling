package com.teamname.crawling.whereToStudy.util;

public class PathConverter {

    public static String convertPath(String originalPath) {
        String osType = OSUtil.getOS();

        if ("windows".equals(osType)) {
            return originalPath.replace("/", "\\");
        } else if ("unix".equals(osType)) {
            return originalPath.replace("\\", "/");
        } else {
            throw new UnsupportedOperationException("Unknown operating system");
        }
    }
}
