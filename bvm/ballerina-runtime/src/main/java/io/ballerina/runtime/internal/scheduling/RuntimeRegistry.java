/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.ObjectValue;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

import static io.ballerina.runtime.api.values.BError.ERROR_PRINT_PREFIX;

/**
 * The registry for runtime dynamic listeners and stop handlers.
 *
 * @since 2201.2.0
 */
public class RuntimeRegistry {

    public final Scheduler scheduler;
    public final Deque<BObject> listenerQueue = new ArrayDeque<>();
    private final Deque<BFunctionPointer> stopHandlerQueue = new ArrayDeque<>();
    private final ReentrantLock listenerLock = new ReentrantLock();
    private final ReentrantLock stopHandlerLock = new ReentrantLock();

    private static final PrintStream outStream = System.err;

    public RuntimeRegistry(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void registerListener(BObject listener) {
        try {
            listenerLock.lock();
            listenerQueue.add(listener);
        } finally {
            listenerLock.unlock();
        }
    }

    public void deregisterListener(BObject listener) {
        try {
            listenerLock.lock();
            listenerQueue.remove(listener);
        } finally {
            listenerLock.unlock();
        }
    }

    public void registerStopHandler(BFunctionPointer stopHandler) {
        try {
            stopHandlerLock.lock();
            stopHandlerQueue.push(stopHandler);
        } finally {
            stopHandlerLock.unlock();
        }
    }

    public void gracefulStop(Strand strand) {
        while (!listenerQueue.isEmpty()) {
            invokeListenerGracefulStop(strand, (ObjectValue) listenerQueue.pollFirst());
        }
        while (!stopHandlerQueue.isEmpty()) {
            invokeStopHandlerFunction(stopHandlerQueue.pollLast());
        }
    }

    private void invokeListenerGracefulStop(Strand strand, ObjectValue listener) {
        try {
            Object result = listener.call(strand, "gracefulStop");
//            RuntimeUtils.handleRuntimeErrorReturns(result);
        } catch (Throwable error) {
//            RuntimeUtils.handleRuntimeErrorReturns(error);
        }
    }

    private void invokeStopHandlerFunction(BFunctionPointer bFunctionPointer) {
        try {
            Object result = bFunctionPointer.call();
//            RuntimeUtils.handleRuntimeErrorReturns(result);
        } catch (Throwable error) {
//            RuntimeUtils.handleRuntimeErrorReturns(error);
        }
    }
}
