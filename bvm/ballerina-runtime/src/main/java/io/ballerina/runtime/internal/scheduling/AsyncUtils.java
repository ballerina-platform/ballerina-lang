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
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Util functions for async invocations.
 */
public class AsyncUtils {

    public static Object handleNonIsolatedStrand(Strand strand, Supplier<Boolean> conditionSupplier,
                                                 Supplier<?> resultSupplier) {
        while (!conditionSupplier.get()) {
            strand.yield();
            strand.resume();
        }
        return resultSupplier.get();
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for future.
     */
    public static Object handleWait(Strand strand, FutureValue future) {
        return handleWait(strand, future.completableFuture);
    }

    public static Object handleWait(Strand strand, CompletableFuture<?> completableFuture) {
        if (strand.isIsolated) {
            return getFutureResult(completableFuture);
        }
        return handleNonIsolatedStrand(strand, completableFuture::isDone,() -> getFutureResult(completableFuture));
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for any of future from given list.
     */
    public static Object handleWaitAny(Strand strand, List<FutureValue> futures) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[futures.size()];
        for (int i = 0; i < futures.size(); i++) {
            completableFutures[i] = futures.get(i).completableFuture;
        }
        return handleWaitAny(strand, completableFutures);
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for all futures from given list.
     */
    public static void handleWaitMultiple(Strand strand, Map<String, FutureValue> keyValues,
                                          MapValue<BString, Object> target) {
        if (strand.isIsolated) {
            getAllFutureResult(keyValues, target);
        }
        handleNonIsolatedStrand(strand, () -> {
            for (FutureValue value : keyValues.values()) {
                if (!value.completableFuture.isDone()) {
                    return false;
                }
            }
            return true;
        }, () -> {
            getAllFutureResult(keyValues, target);
            return null;
        });
    }

    public static Object handleWaitAny(Strand strand, CompletableFuture<?>[] completableFutures) {
        Object result;
        if (strand.isIsolated) {
            result = getAnyFutureResult(completableFutures);
        } else {
            result = handleNonIsolatedStrand(strand, () -> {
                for (CompletableFuture<?> completableFuture : completableFutures) {
                    if (completableFuture.isDone()) {
                        return true;
                    }
                }
                return false;
            }, () -> getAnyFutureResult(completableFutures));
        }

        if (completableFutures.length > 1 && result instanceof BError) {
            List<CompletableFuture<?>> nonErrorFutures = new ArrayList<>();
            for (CompletableFuture<?> completableFuture : completableFutures) {
                if (completableFuture.isDone()) {
                    result = getFutureResult(completableFuture);
                    if(!(getFutureResult(completableFuture) instanceof BError)) {
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

    public static Object getFutureResult(CompletableFuture<?> completableFuture) {
        try {
            return completableFuture.get();
        } catch (BError e) {
            return e;
        } catch (Throwable e) {
            if (e.getCause() instanceof  BError error) {
                return error;
            }
            return ErrorCreator.createError(e);
        }
    }

    public static Object getAnyFutureResult(CompletableFuture<?>[] completableFutures) {
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(completableFutures);
        return getFutureResult(anyFuture);
    }

    private static void getAllFutureResult(Map<String, FutureValue> keyValues, MapValue<BString, Object> target) {
        for (Map.Entry<String, FutureValue> entry : keyValues.entrySet()) {
            FutureValue future = entry.getValue();
            target.put(StringUtils.fromString(entry.getKey()), getFutureResult(future.completableFuture));
        }
    }

    private AsyncUtils() {
    }
}
