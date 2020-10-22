/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.values.ObjectValue;

/**
 * This class is used for Java interoperability tests.
 * <p>
 * Schedule a ballerina function to run multiple times.
 *
 * @since 1.0.0
 */
public class Timer {

    public static void startTimer(Environment env, int interval, int count, ObjectValue object) {
        Runtime runtime = env.getRuntime();

        new Thread(() -> {
            for (int i = 0; i < count; i++) {
                sleep(interval);
                runtime.invokeMethodAsync(object, "exec", null, null, null);
            }
        }).start();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            assert false;
        }
    }
}
