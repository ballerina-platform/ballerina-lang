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
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.utils.StringUtils;

import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Extern function sleep.
 *
 * @since 2.0.0
 */
public final class Utils {

    private static final int CORE_THREAD_POOL_SIZE = 1;

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(CORE_THREAD_POOL_SIZE);

    private Utils() {
    }

    public static void sleep(Environment env, long delayMillis) {
        Future balFuture = env.markAsync();
        EXECUTOR.schedule(() -> balFuture.complete(null), delayMillis, TimeUnit.MILLISECONDS);
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
