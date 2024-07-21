/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.CountDownLatch;

/**
 * This class used to handle ballerina function invocation synchronously.
 *
 * @since 2201.9.1
 */
public class SyncCallback implements Callback {

    public CountDownLatch latch;
    public BError initError;

    public SyncCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void notifySuccess(Object result) {
        latch.countDown();
    }

    @Override
    public void notifyFailure(BError error) {
        latch.countDown();
        initError = error;
    }
}
