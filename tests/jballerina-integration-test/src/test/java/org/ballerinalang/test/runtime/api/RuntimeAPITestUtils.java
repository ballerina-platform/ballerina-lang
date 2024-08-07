/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.runtime.api;

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

/**
 * Contains utils methods required for test Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.1
 */
public class RuntimeAPITestUtils {

    private static final PrintStream out = System.out;

    public static void blockAndInvokeMethodAsync(Runtime balRuntime, String functionName, Object... args) {
        final CountDownLatch latch = new CountDownLatch(1);
        balRuntime.invokeMethodAsync(functionName, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                out.println(result);
                latch.countDown();
            }

            @Override
            public void notifyFailure(BError error) {
                out.println("Error: " + error);
                latch.countDown();
            }
        }, args);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ErrorCreator.createError(e);
        }
    }

    public static void blockAndInvokeMethodAsyncSequentially(Runtime balRuntime, BObject object, String functionName,
                                                             Type returnType, Object... args) {
        final CountDownLatch latch = new CountDownLatch(1);
        balRuntime.invokeMethodAsyncSequentially(object, functionName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                latch.countDown();
                out.println(result);
            }

            @Override
            public void notifyFailure(BError error) {
                latch.countDown();
                out.println("Error: " + error);
            }
        }, null, returnType, args);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw ErrorCreator.createError(e);
        }
    }
}
