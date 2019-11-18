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

package org.ballerinalang.nats.basic.consumer;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.nats.Constants.ON_ERROR_RESOURCE;

/**
 * Handles dispatching errors detected by the error listener and due to data binding.
 *
 * @since 1.0.0
 */
public class ErrorHandler {

    /**
     * Dispatch errors to the onError resource, if the onError resource is available.
     *
     * @param serviceObject ObjectValue service
     * @param msgObj        Message object
     * @param e             ErrorValue
     */
    static void dispatchError(ObjectValue serviceObject, ObjectValue msgObj, ErrorValue e, BRuntime runtime) {
        boolean onErrorResourcePresent = Arrays.stream(serviceObject.getType().getAttachedFunctions())
                .anyMatch(resource -> resource.getName().equals(ON_ERROR_RESOURCE));
        if (onErrorResourcePresent) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            runtime.invokeMethodAsync(serviceObject, ON_ERROR_RESOURCE,
                    new DefaultMessageHandler.ResponseCallback(countDownLatch), msgObj, true, e, true);
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw Utils.createNatsError(Constants.THREAD_INTERRUPTED_ERROR);
            }
        }
    }

    private ErrorHandler() {
    }
}
