/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.utils.interop;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;

import java.io.PrintStream;

/**
 * Extern function sleep.
 *
 * @since 2.0.0
 */
public class Utils {

    public static void sleep(Environment env, long delayMillis) {
        env.yieldAndRun(() -> {
            try {
                Thread.sleep(delayMillis);
                return null;
            } catch (InterruptedException e) {
                throw ErrorCreator.createError(e);
            }
        });
    }

    public static boolean isIsolated() {
        return Scheduler.getStrand().isIsolated;
    }

    public static void print(Object... values) {
        PrintStream out = System.out;
        if (values == null) {
            out.print((Object) null);
            return;
        }
        for (Object value : values) {
            if (value != null) {
                out.print(StringUtils.getStringValue(value));
            }
        }
    }

    public static void println(Object... values) {
        PrintStream out = System.out;
        if (values == null) {
            out.println((Object) null);
            return;
        }
        StringBuilder content = new StringBuilder();
        for (Object value : values) {
            if (value != null) {
                content.append(StringUtils.getStringValue(value));
            }
        }
        out.println(content);
    }
}
