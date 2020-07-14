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

import org.ballerinalang.jvm.scheduling.LogContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.logging.util.BLogLevel;

/**
 * Native function implementations of the log-api module.
 *
 * @since 1.1.0
 */
public class Utils extends AbstractLogFunction {

    private static String logContext = "logContext";

    public static void printDebug(Object msg) {
        if (logLevelEnabled(BLogLevel.DEBUG, Scheduler.getStrand(), getPackagePath())) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.DEBUG, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).debug(message);
                    });
        }
    }

    public static void printError(Object msg, Object err) {
        if (logLevelEnabled(BLogLevel.ERROR, Scheduler.getStrand(), getPackagePath())) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.ERROR, getPackagePath(),
                    (pkg, message) -> {
                        String errorMsg = (err == null) ? "" : " : " + err.toString();
                        getLogger(pkg).error(message + errorMsg);
                    });
        }
    }

    public static void printInfo(Object msg) {
        if (logLevelEnabled(BLogLevel.INFO, Scheduler.getStrand(), getPackagePath())) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.INFO, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).info(message);
                    });
        }
    }

    public static void printTrace(Object msg) {
        if (logLevelEnabled(BLogLevel.TRACE, Scheduler.getStrand(), getPackagePath())) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.TRACE, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).trace(message);
                    });
        }
    }

    public static void printWarn(Object msg) {
        if (logLevelEnabled(BLogLevel.WARN, Scheduler.getStrand(), getPackagePath())) {
            logMessage(Scheduler.getStrand(), msg, BLogLevel.WARN, getPackagePath(),
                    (pkg, message) -> {
                        getLogger(pkg).warn(message);
                    });
        }
    }

    private static boolean logLevelEnabled(BLogLevel logLevel, Strand strand, String pkg) {
        LogContext logContext = (LogContext) strand.getProperty(Utils.logContext);
        if (logContext != null) {
            if (logContext.getLogLevel() != null) {
                return logContext.getLogLevel().value() <= logLevel.value();
            }
        } else if (strand.parent != null) {
            LogContext parentLogContext = (LogContext) strand.parent.getProperty(Utils.logContext);
            if (parentLogContext != null) {
                if (parentLogContext.getLogLevel() != null && parentLogContext.isPropagate()) {
                    logContext = new LogContext(parentLogContext.getLogLevel(), true);
                    strand.setProperty(Utils.logContext, logContext);
                    return parentLogContext.getLogLevel().value() <= logLevel.value();
                }
            }
        }
        if (LOG_MANAGER.isModuleLogLevelEnabled()) {
            return LOG_MANAGER.getPackageLogLevel(pkg).value() <= logLevel.value();
        } else {
            return LOG_MANAGER.getPackageLogLevel(".").value() <= logLevel.value();
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

    public static void setStrandLogLevel(BString logLevel, boolean propagate) {
        String level = logLevel.getValue();
        LogContext logContext = new LogContext(BLogLevel.toBLogLevel(level), propagate);
        Scheduler.getStrand().setProperty(Utils.logContext, logContext);
    }
}
