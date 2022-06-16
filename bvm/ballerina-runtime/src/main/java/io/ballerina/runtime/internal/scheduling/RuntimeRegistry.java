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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.values.FutureValue;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

/**
 * The registry for runtime dynamic listeners and stop handlers.
 *
 * @since 2201.2.0
 */
public class RuntimeRegistry {

    private final Scheduler scheduler;
    private final Set<BObject> listenerSet = new HashSet<>();
    private final Stack<BFunctionPointer<?, ?>> stopHandlerStack = new Stack<>();

    private static final PrintStream outStream = System.err;
    public static final String ERROR_PRINT_PREFIX = "error: ";

    public RuntimeRegistry(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void stopDynamic(Strand strand) {
        stopListeners(strand);
        moduleGracefulStop(strand);
    }

    public synchronized void registerListener(BObject listener) {
        listenerSet.add(listener);
        scheduler.setImmortal(true);
    }

    public synchronized void deregisterListener(BObject listener) {
        listenerSet.remove(listener);
        if (!scheduler.isListenerDeclarationFound() && listenerSet.isEmpty()) {
            scheduler.setImmortal(false);
        }
    }

    public synchronized void stopListeners(Strand strand) {
        if (listenerSet.isEmpty()) {
            return;
        }
        AsyncUtils.blockStrand(strand);
        invokeMethodAsync(strand, Scheduler.getStrand().scheduler);
    }

    private void invokeMethodAsync(Strand strand, Scheduler scheduler) {
        Iterator<BObject> itr = listenerSet.iterator();
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                if (itr.hasNext()) {
                    invokeMethodAsync(strand, scheduler);
                } else {
                    unblockStrand();
                }
            }

            @Override
            public void notifyFailure(BError error) {
                outStream.println(ERROR_PRINT_PREFIX + error.getPrintableStackTrace());
                unblockStrand();
            }
        };

        BObject listener = itr.next();
        invokeMethodAsyncConcurrently(listener, strand, callback, strand.getMetadata(), scheduler);
    }

    private void invokeMethodAsyncConcurrently(BObject listener, Strand parent, AsyncFunctionCallback callback,
                                               StrandMetadata metadata, Scheduler scheduler) {
        try {
            Function<?, ?> func = o ->  listener.call((Strand) ((Object[]) o)[0], "gracefulStop");
            AsyncUtils.blockStrand(parent);
            FutureValue future = scheduler.createFuture(parent, null, null, PredefinedTypes.TYPE_NULL,
                    null, metadata);
            future.callback = callback;
            callback.setFuture(future);
            callback.setStrand(parent);
            scheduler.schedule(new Object[1], func, future);
        } catch (BError error) {
            callback.notifyFailure(error);
        } catch (Throwable t) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(t.getMessage())));
        }
    }

    public synchronized void registerStopHandler(BFunctionPointer<?, ?> stopHandler) {
        stopHandlerStack.push(stopHandler);
    }

    public synchronized void moduleGracefulStop(Strand strand) {
        if (stopHandlerStack.isEmpty()) {
            return;
        }
        AsyncUtils.blockStrand(strand);
        scheduleNext(strand);
    }

    private void scheduleNext(Strand strand) {
        BFunctionPointer<?, ?> bFunctionPointer = stopHandlerStack.pop();
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                if (!stopHandlerStack.isEmpty()) {
                    scheduleNext(strand);
                } else {
                    unblockStrand();
                }
            }

            @Override
            public void notifyFailure(BError error) {
                outStream.println(ERROR_PRINT_PREFIX + error.getPrintableStackTrace());
                unblockStrand();
            }
        };
        AsyncUtils.invokeFunctionPointerAsync(bFunctionPointer, strand, null, strand.getMetadata(),
                new Object[]{strand}, callback, scheduler);
    }
}
