/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler;

import java.util.HashMap;
import java.util.List;

public class LoggerRegistry {
    private static HashMap<String, Logger> loggers = new HashMap<>();

    static void registerLogger(String loggerName, Logger logger) {
        loggers.put(loggerName, logger);
    }

    public static void triggerCompileStarted() {
        for (Logger logger : loggers.values()) {
            logger.compileStarted();
        }
    }

    public static void triggerExecutablesGenerated() {
        for (Logger logger : loggers.values()) {
            logger.executablesGenerated();
        }
    }

    public static void triggerExecutableGenerated() {
        for (Logger logger : loggers.values()) {
            logger.executableGenerated();
        }
    }

    public static void triggerExecutableGenerated(String fileName) {
        for (Logger logger : loggers.values()) {
            logger.executableGenerated(fileName);
        }
    }

    public static void triggerPackageCompiled(String pkg) {
        for (Logger logger : loggers.values()) {
            logger.packageCompiled(pkg);
        }
    }

    public static void triggerTestSuiteResultGenerated(int passed, int failed, int skipped) {
        for (Logger logger : loggers.values()) {
            logger.testSuiteResultGenerated(passed, failed, skipped);
        }
    }

    public static void triggerTestsCompleted() {
        for (Logger logger : loggers.values()) {
            logger.testsCompleted();
        }
    }

    public static void triggerTestsCompiled() {
        for (Logger logger : loggers.values()) {
            logger.testsCompiled();
        }
    }

    public static void triggerTestsFailed(String functionName, String error) {
        for (Logger logger : loggers.values()) {
            logger.testsFailed(functionName, error);
        }
    }

    public static void triggerTestsPassed(String functionName) {
        for (Logger logger : loggers.values()) {
            logger.testsPassed(functionName);
        }
    }

    public static void triggerTestsNotFound() {
        for (Logger logger : loggers.values()) {
            logger.testsNotFound();
        }
    }

    public static void triggerTestsNotFoundInSuite() {
        for (Logger logger : loggers.values()) {
            logger.testsNotFoundInSuite();
        }
    }

    public static void triggerNoTestGroupsAvailable() {
        for (Logger logger : loggers.values()) {
            logger.noTestGroupsAvailable();
        }
    }

    public static void triggerTestGroupsAvailable(List<String> groupList) {
        for (Logger logger : loggers.values()) {
            logger.testGroupsAvailable(groupList);
        }
    }

    public static void triggerBeforeAndAfterTestsFailed(String errorMsg) {
        for (Logger logger : loggers.values()) {
            logger.beforeAndAfterTestsFailed(errorMsg);
        }
    }

    public static void triggerLineBreak() {
        for (Logger logger : loggers.values()) {
            logger.lineBreak();
        }
    }
}
