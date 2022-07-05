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
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.values.FutureValue;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import static io.ballerina.runtime.api.values.BError.ERROR_PRINT_PREFIX;

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

    public RuntimeRegistry(Scheduler scheduler) {
        this.scheduler = scheduler;
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

    public synchronized void registerStopHandler(BFunctionPointer<?, ?> stopHandler) {
        stopHandlerStack.push(stopHandler);
    }

    public synchronized void gracefulStop(Strand strand) {
        Scheduler scheduler = strand.scheduler;
        if (listenerSet.isEmpty() && stopHandlerStack.isEmpty()) {
            return;
        }

        if (listenerSet.isEmpty()) {
            invokeStopHandlerFunction(strand, scheduler);
        } else {
            invokeListenerGracefulStop(strand, scheduler);
        }
    }

    private void invokeListenerGracefulStop(Strand strand, Scheduler scheduler) {
        Iterator<BObject> itr = listenerSet.iterator();
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                if (itr.hasNext()) {
                    invokeListenerGracefulStop(strand, scheduler);
                } else {
                    if (stopHandlerStack.isEmpty()) {
                        return;
                    }
                    invokeStopHandlerFunction(strand, scheduler);
                }
            }

            @Override
            public void notifyFailure(BError error) {
                outStream.println(ERROR_PRINT_PREFIX + error.getPrintableStackTrace());
            }
        };

        BObject listener = itr.next();
        Function<?, ?> func = o ->  listener.call((Strand) ((Object[]) o)[0], "gracefulStop");
        callback.setStrand(strand);
        scheduler.schedule(new Object[1], func, null, callback, null, null,
                null, strand.getMetadata());
    }

    private void invokeStopHandlerFunction(Strand strand, Scheduler scheduler) {
        BFunctionPointer<?, ?> bFunctionPointer = stopHandlerStack.pop();
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                if (stopHandlerStack.isEmpty()) {
                    return;
                }
                invokeStopHandlerFunction(strand, scheduler);
            }

            @Override
            public void notifyFailure(BError error) {
                outStream.println(ERROR_PRINT_PREFIX + error.getPrintableStackTrace());
            }
        };
        final FutureValue future = scheduler.createFuture(strand, callback, null,
                ((BFunctionType) bFunctionPointer.getType()).retType, null, strand.getMetadata());
        callback.setStrand(strand);
        scheduler.scheduleLocal(new Object[]{strand}, bFunctionPointer, strand, future);
    }
}
