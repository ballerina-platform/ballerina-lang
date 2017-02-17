/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.lang.system;

import org.slf4j.Logger;

/**
 * A class to hold log related util methods.
 */
public class LogUtil {

    /**
     * Log given value in specified log level using provided logger instance.
     *
     * @param logger   Log instance
     * @param logLevel log level whether debug, info etc
     * @param value    String value that need to log
     */
    public static void log(Logger logger, int logLevel, String value) {
        switch (logLevel) {
        case 1:
            logTrace(logger, value);
            break;
        case 2:
            logDebug(logger, value);
            break;
        case 3:
            logInfo(logger, value);
            break;
        case 4:
            logWarn(logger, value);
            break;
        case 5:
            logError(logger, value);
            break;
        default:
        }
    }

    private static void logTrace(Logger logger, String s) {
        logger.trace("[TRACE] " + s);
    }

    private static void logDebug(Logger logger, String s) {
        logger.debug("[DEBUG] " + s);
    }

    private static void logInfo(Logger logger, String s) {
        logger.info("[INFO] " + s);
    }

    private static void logWarn(Logger logger, String s) {
        logger.warn("[WARN] " + s);
    }

    private static void logError(Logger logger, String s) {
        logger.error("[ERROR] " + s);
    }
}
