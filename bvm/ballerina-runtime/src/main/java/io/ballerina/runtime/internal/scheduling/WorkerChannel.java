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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents a worker channel that is created for each worker to worker interaction.
 *
 * @since 2201.11.0
 */

public class WorkerChannel {

    public final String name;
    public AtomicInteger doneCount;
    final CompletableFuture<Object> resultFuture;
    final CompletableFuture<Void> readFuture;
    private Object result;

    public WorkerChannel(String name) {
        this.name = name;
        this.resultFuture = new CompletableFuture<>();
        this.readFuture = new CompletableFuture<>();
        this.doneCount = new AtomicInteger(2);
    }

    public Object read() {
        result = AsyncUtils.getFutureResult(readFuture);
        readFuture.complete(null);
        return result;
    }

    public Object getResult() {
        return result;
    }

    public void write(Object result) {
        resultFuture.complete(result);
    }

    public void panic(BError error) {
        resultFuture.completeExceptionally(error);
    }
}
