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

public class ListenerRegistry {
    private static HashMap<String, Listener> listeners = new HashMap<>();

    static void registerListener(String listenerName, Listener listener) {
        listeners.put(listenerName, listener);
    }

    public static void triggerCompileStarted() {
        for (Listener listener : listeners.values()) {
            listener.compileStarted();
        }
    }

    public static void triggerExecutablesGenerated() {
        for (Listener listener : listeners.values()) {
            listener.executablesGenerated();
        }
    }

    public static void triggerExecutableGenerated() {
        for (Listener listener : listeners.values()) {
            listener.executableGenerated();
        }
    }

    public static void triggerExecutableGenerated(String fileName) {
        for (Listener listener : listeners.values()) {
            listener.executableGenerated(fileName);
        }
    }

    public static void triggerPackageCompiled(String pkg) {
        for (Listener listener : listeners.values()) {
            listener.packageCompiled(pkg);
        }
    }

    public static void triggerTestSuiteResultGenerated(int passed, int failed, int skipped) {
        for (Listener listener : listeners.values()) {
            listener.testSuiteResultGenerated(passed, failed, skipped);
        }
    }

    public static void triggerTestsCompleted() {
        for (Listener listener : listeners.values()) {
            listener.testsCompleted();
        }
    }

    public static void triggerTestsCompiled() {
        for (Listener listener : listeners.values()) {
            listener.testsCompiled();
        }
    }

    public static void triggerTestsFailed(String functionName, String error) {
        for (Listener listener : listeners.values()) {
            listener.testsFailed(functionName, error);
        }
    }

    public static void triggerTestsPassed(String functionName) {
        for (Listener listener : listeners.values()) {
            listener.testsPassed(functionName);
        }
    }

    public static void triggerTestsNotFound() {
        for (Listener listener : listeners.values()) {
            listener.testsNotFound();
        }
    }

    public static void triggerTestsNotFoundInSuite() {
        for (Listener listener : listeners.values()) {
            listener.testsNotFoundInSuite();
        }
    }

    public static void triggerNoTestGroupsAvailable() {
        for (Listener listener : listeners.values()) {
            listener.noTestGroupsAvailable();
        }
    }

    public static void triggerTestGroupsAvailable(List<String> groupList) {
        for (Listener listener : listeners.values()) {
            listener.testGroupsAvailable(groupList);
        }
    }

    public static void triggerBeforeAndAfterTestsFailed(String errorMsg) {
        for (Listener listener : listeners.values()) {
            listener.beforeAndAfterTestsFailed(errorMsg);
        }
    }

    public static void triggerLineBreak() {
        for (Listener listener : listeners.values()) {
            listener.lineBreak();
        }
    }
}
