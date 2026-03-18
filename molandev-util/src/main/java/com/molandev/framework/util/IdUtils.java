package com.molandev.framework.util;

import java.util.UUID;

/**
 *
 */
public class IdUtils {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
