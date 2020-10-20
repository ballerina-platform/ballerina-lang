/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * A mock listener for testing services. It can be used to invoke a resource in the service.
 */
public class MockListener {

    private static BObject service;
    private static BError err;

    public static Object attach(BObject servObj) {
        service = servObj;
        return null;
    }

    public static Object invokeResource(Environment env, BString name) throws InterruptedException {
        if (service != null) {
            CountDownLatch latch = new CountDownLatch(1);
            Runtime runtime = env.getRuntime();
            runtime.invokeMethodAsync(service, name.getValue(), null, null,
                                      new Callback() {
                                          @Override
                                          public void notifySuccess() {
                                              latch.countDown();
                                          }

                                          @Override
                                          public void notifyFailure(BError error) {
                                              err = error;
                                              latch.countDown();
                                          }
                             }, new HashMap<>(), null);
            latch.await();
        }
        return err;
    }
}
