/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.log;

import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.logging.util.BLogLevel;

/**
 * Native function implementations of the log-api module.
 *
 * @since 1.1.0
 */
public class Utils extends AbstractLogFunction {

    public static void printDebug(Object msg) {
        boolean logLevelEnabled;
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(getPackagePath()).value() <= BLogLevel.DEBUG.value();
        } else {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(".").value() <= BLogLevel.DEBUG.value();
        }
        if (logLevelEnabled) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.DEBUG, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).debug(message);
                    });
        }
    }

    public static void printError(Object msg, Object err) {
        boolean logLevelEnabled;
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(getPackagePath()).value() <= BLogLevel.ERROR.value();
        } else {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(".").value() <= BLogLevel.ERROR.value();
        }
        if (logLevelEnabled) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.ERROR, getPackagePath(),
                    (pkg, message) -> {
                        String errorMsg = (err == null) ? "" : " : " + err.toString();
                        getLogger(pkg).error(message + errorMsg);
                    });
        }
    }

    public static void printInfo(Object msg) {
        boolean logLevelEnabled;
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(getPackagePath()).value() <= BLogLevel.INFO.value();
        } else {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(".").value() <= BLogLevel.INFO.value();
        }
        if (logLevelEnabled) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.INFO, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).info(message);
                    });
        }
    }

    public static void printTrace(Object msg) {
        boolean logLevelEnabled;
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(getPackagePath()).value() <= BLogLevel.TRACE.value();
        } else {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(".").value() <= BLogLevel.TRACE.value();
        }
        if (logLevelEnabled) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.TRACE, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).trace(message);
                    });
        }
    }

    public static void printWarn(Object msg) {
        boolean logLevelEnabled;
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(getPackagePath()).value() <= BLogLevel.WARN.value();
        } else {
            logLevelEnabled = LOG_MANAGER.getPackageLogLevel(".").value() <= BLogLevel.WARN.value();
        }
        if (logLevelEnabled) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.WARN, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).warn(message);
                    });
        }
    }

    public static void setModuleLogLevel(BString logLevel, Object moduleName) {
        String module;
        if (moduleName == null) {
            module = getPackagePath();
        } else {
            module = moduleName.toString();
        }
        String level = logLevel.getValue();
        LOG_MANAGER.setModuleLogLevel(BLogLevel.toBLogLevel(level), module);
    }
}
