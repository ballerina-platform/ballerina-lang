/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.ErrorUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents a worker channel that is created for each worker to worker interaction.
 *
 * @since 2201.11.0
 */

public class WorkerChannel {

    private final String name;
    private final AtomicInteger doneCount;
    private final CompletableFuture<Object> resultFuture;
    private final CompletableFuture<Object> receiveFuture;
    private boolean cancel;

    public WorkerChannel(String name) {
        this.name = name;
        this.resultFuture = new CompletableFuture<>();
        this.receiveFuture = new CompletableFuture<>();
        this.doneCount = new AtomicInteger(2);
        this.cancel = false;
    }

    public Object read() {
        if (cancel) {
            throw ErrorUtils.createCancelledFutureError();
        }
        try {
            return AsyncUtils.getFutureResult(resultFuture);
        } finally {
            receiveFuture.complete(null);
        }
    }

    public void write(Object result) {
        if (cancel) {
            throw ErrorUtils.createCancelledFutureError();
        }
        resultFuture.complete(result);
    }

    public void panicOnSend(BError error) {
        if (resultFuture.isDone()) {
            return;
        }
        resultFuture.completeExceptionally(error);
    }

    public void panicOnReceive(BError error) {
        if (receiveFuture.isDone()) {
            return;
        }
        receiveFuture.completeExceptionally(error);
    }

    public void errorOnSend(String channelKey, Object returnValue) {
        if (resultFuture.isDone()) {
            return;
        }
        BError bError;
        if (returnValue instanceof BError error) {
            bError = error;
        } else {
            bError = ErrorUtils.createNoMessageError(channelKey);
        }
        resultFuture.complete(bError);
    }

    public void errorOnReceive(String channelKey, Object returnValue) {
        if (receiveFuture.isDone()) {
            return;
        }
        BError bError;
        if (returnValue instanceof BError error) {
            bError = error;
        } else {
            bError = ErrorUtils.createNoMessageError(channelKey);
        }
        receiveFuture.complete(bError);
    }

    public boolean isWritten() {
        return resultFuture.isDone();
    }

    public boolean isReceived() {
        return receiveFuture.isDone();
    }

    public boolean done() {
        return doneCount.incrementAndGet() == 0;
    }

    public void cancel() {
        this.cancel = true;
    }

    public String getName() {
        return name;
    }

    public CompletableFuture<Object> getResultFuture() {
        return resultFuture;
    }

    public CompletableFuture<Object> getReceiveFuture() {
        return receiveFuture;
    }
}
