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

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BObject;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The registry for runtime dynamic listeners and stop handlers.
 *
 * @since 2201.2.0
 */
public class RuntimeRegistry {

    private final Scheduler scheduler;
    private final Set<BObject> listenerSet = new HashSet<>();
    private final Stack<BFunctionPointer<?, ?>> stopHandlerStack = new Stack<>();

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

    public synchronized void stopListeners(Strand strand) {
        for (BObject listener : listenerSet) {
            Future future = new Future(strand);
            strand.blockedOnExtern = true;
            strand.setState(State.BLOCK_AND_YIELD);

            Callback callback = new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    future.complete(result);
                }

                @Override
                public void notifyFailure(BError error) {
                    future.complete(error);
                }
            };

            invokeMethodAsyncConcurrently(listener, strand, callback, strand.getMetadata());
        }
    }

    private void invokeMethodAsyncConcurrently(BObject object, Strand strand, Callback callback,
                                               StrandMetadata metadata) {
        try {
            Function<?, ?> func = o -> object.call(strand, "gracefulStop");
            scheduler.schedule(new Object[1], func, null, callback, null,
                    null, null, metadata);
        } catch (BError e) {
            callback.notifyFailure(e);
        } catch (Throwable e) {
            callback.notifyFailure(ErrorCreator.createError(StringUtils.fromString(e.getMessage())));
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
        scheduleNext(strand, strand.getMetadata(), () -> new Object[]{strand}, result -> { },
                () -> null, strand.scheduler);
    }

    private void scheduleNext(Strand strand, StrandMetadata metadata, Supplier<Object[]> argsSupplier,
                              Consumer<Object> futureResult, Supplier<Object> returnValueSupplier,
                              Scheduler scheduler) {
        BFunctionPointer<?, ?> bFunctionPointer = stopHandlerStack.pop();
        AsyncFunctionCallback callback = new AsyncFunctionCallback() {
            @Override
            public void notifySuccess(Object result) {
                futureResult.accept(getFutureResult());
                if (!stopHandlerStack.isEmpty()) {
                    scheduleNext(strand, metadata, argsSupplier, futureResult, returnValueSupplier, scheduler);
                } else {
                    setReturnValues(returnValueSupplier.get());
                }
            }

            @Override
            public void notifyFailure(BError error) {
                handleRuntimeErrors(error);
            }
        };
        AsyncUtils.invokeFunctionPointerAsync(bFunctionPointer, strand, null, metadata, argsSupplier.get(),
                callback, scheduler);
    }
}
