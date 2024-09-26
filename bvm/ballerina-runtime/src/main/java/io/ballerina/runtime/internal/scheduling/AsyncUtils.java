/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Util functions for async invocations.
 */
public class AsyncUtils {

    public static Object handleNonIsolatedStrand(Strand strand, Supplier<Boolean> conditionSupplier,
                                                 Supplier<?> resultSupplier) {
        boolean waitDone = false;
        while (!waitDone) {
            try {
                strand.yield();
                waitDone = conditionSupplier.get();
            } finally {
                strand.resume();
            }
        }
        return resultSupplier.get();
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for future.
     */
    public static Object handleWait(Strand strand, FutureValue future) {
        if (future.getAndSetWaited()) {
            return ErrorUtils.createWaitOnSameFutureError();
        }
        return handleWait(strand, future.completableFuture);
    }

    public static Object handleWait(Strand strand, CompletableFuture<Object> completableFuture) {
        if (strand.isIsolated) {
            return getFutureResult(completableFuture);
        }
        return handleNonIsolatedStrand(strand, completableFuture::isDone, () -> getFutureResult(completableFuture));
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for any of future from given list.
     */
    public static Object handleWaitAny(Strand strand, List<FutureValue> futures) {
        CompletableFuture<?>[] cFutures = new CompletableFuture[futures.size()];
        for (int i = 0; i < futures.size(); i++) {
            FutureValue future = futures.get(i);
            if (future.getAndSetWaited()) {
                return ErrorUtils.createWaitOnSameFutureError();
            }
            cFutures[i] = future.completableFuture;
        }
        return handleWaitAny(strand, cFutures);
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for all futures from given list.
     */
    public static void handleWaitMultiple(Strand strand, Map<String, FutureValue> futureMap,
                                          MapValue<BString, Object> target) {
        Collection<FutureValue> futures = futureMap.values();
        List<CompletableFuture<?>> cFutures = new ArrayList<>();
        List<String> alreadyWaitedKeys = new ArrayList<>();
        for (Map.Entry<String, FutureValue> entry : futureMap.entrySet()) {
            FutureValue future = entry.getValue();
            if (!future.getAndSetWaited()) {
                cFutures.add(future.completableFuture);
            } else {
                alreadyWaitedKeys.add(entry.getKey());
            }
        }
        if (strand.isIsolated) {
            waitForAllFutureResult(cFutures.toArray(new CompletableFuture[0]));
            getAllFutureResult(futureMap, alreadyWaitedKeys, target);
        }
        handleNonIsolatedStrand(strand, () -> {
            for (CompletableFuture<?> cFuture : cFutures) {
                if (cFuture.isCompletedExceptionally()) {
                    getFutureResult(cFuture);
                }
                if (!cFuture.isDone()) {
                    return false;
                }
            }
            return true;
        }, () -> {
            getAllFutureResult(futureMap, alreadyWaitedKeys, target);
            return null;
        });
    }

    public static Object handleWaitAny(Strand strand, CompletableFuture<?>[] cFutures) {
        Object result;
        if (strand.isIsolated) {
            result = getAnyFutureResult(cFutures);
        } else {
            result = handleNonIsolatedStrand(strand, () -> {
                for (CompletableFuture<?> completableFuture : cFutures) {
                    if (completableFuture.isDone()) {
                        return true;
                    }
                }
                return false;
            }, () -> getAnyFutureResult(cFutures));
        }

        if (cFutures.length > 1 && result instanceof BError) {
            List<CompletableFuture<?>> nonErrorFutures = new ArrayList<>();
            for (CompletableFuture<?> completableFuture : cFutures) {
                if (completableFuture.isDone()) {
                    result = getFutureResult(completableFuture);
                    if (!(result instanceof BError)) {
                        return result;
                    }
                } else {
                    nonErrorFutures.add(completableFuture);
                }
            }
            if (!nonErrorFutures.isEmpty()) {
                return handleWaitAny(strand, nonErrorFutures.toArray(new CompletableFuture<?>[0]));
            }
        }
        return result;
    }

    public static Object getFutureResult(CompletableFuture<?> completableFuture) throws BError {
        try {
            return completableFuture.get();
        } catch (Throwable e) {
            if (e.getCause() instanceof BError bError) {
                throw bError;
            }
            throw ErrorCreator.createError(e);
        }
    }

    public static Object getAnyFutureResult(CompletableFuture<?>[] cFutures) {
        int fSize = cFutures.length;
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        AtomicInteger count = new AtomicInteger();
        for (CompletableFuture<?> f : cFutures) {
            f.whenComplete((result, ex) -> {
                if (ex != null) {
                    resultFuture.completeExceptionally(ex);
                    return;
                }
                if (count.incrementAndGet() < fSize && result instanceof BError) {
                    return;
                }
                resultFuture.complete(result);
            });
        }
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(resultFuture, CompletableFuture.allOf(cFutures));
        Object r = getFutureResult(anyFuture);
        if (r != null) {
            return r;
        }
        return getFutureResult(resultFuture);
    }

    private static void getAllFutureResult(Map<String, FutureValue> futureMap, List<String> alreadyWaitedKeys,
                                           MapValue<BString, Object> target) {
        for (Map.Entry<String, FutureValue> entry : futureMap.entrySet()) {
            FutureValue future = entry.getValue();
            String key = entry.getKey();
            if (alreadyWaitedKeys.contains(key)) {
                target.put(StringUtils.fromString(key), ErrorUtils.createWaitOnSameFutureError());
            } else {
                target.put(StringUtils.fromString(key), getFutureResult(future.completableFuture));
            }
        }
    }

    public static void waitForAllFutureResult(CompletableFuture<?>[] futures) {
        CompletableFuture<?> failure = new CompletableFuture<>();
        for (CompletableFuture<?> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }
        CompletableFuture<Object> future = CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
        getFutureResult(future);
    }

    private AsyncUtils() {
    }
}
