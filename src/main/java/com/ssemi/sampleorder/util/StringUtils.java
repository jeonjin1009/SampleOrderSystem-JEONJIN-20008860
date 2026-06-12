package com.ssemi.sampleorder.util;

public final class StringUtils {
    private StringUtils() {}

    public static String abbreviateId(String id) {
        if (id == null || id.length() <= 8) return id;
        return id.substring(0, 8);
    }
}
