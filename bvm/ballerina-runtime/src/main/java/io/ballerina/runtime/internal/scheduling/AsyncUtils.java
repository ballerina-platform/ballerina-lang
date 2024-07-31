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

/**
 * Util functions for async invocations.
 */
public class AsyncUtils {

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for future.
     */
    public static Object handleWait(FutureValue future) {
        try {
            return future.completableFuture.get();
        } catch (BError e) {
            return e;
        } catch (Throwable e) {
            return ErrorCreator.createError(e);
        }
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for any of future from given list.
     */
    public static Object handleWaitAny(List<FutureValue> futures) {
        CompletableFuture<?>[] completableFutures = new CompletableFuture[futures.size()];
        for (int i = 0; i < futures.size(); i++) {
            completableFutures[i] = futures.get(i).completableFuture;
        }
        return handleWaitAny(completableFutures);
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen wait for all futures from given list.
     */
    public static void handleWaitMultiple(Map<String, FutureValue> keyValues, MapValue<BString, Object> target) {
        for (Map.Entry<String, FutureValue> entry : keyValues.entrySet()) {
            FutureValue future = entry.getValue();
            Object result;
            try {
                result = future.completableFuture.get();
            } catch (BError e) {
                result = e;
            } catch (Throwable e) {
                throw ErrorCreator.createError(e);
            }
            target.put(StringUtils.fromString(entry.getKey()), result);
        }
    }

    private static Object handleWaitAny(CompletableFuture<?>[] completableFutures) {
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(completableFutures);
        Object result;
        try {
            result = anyFuture.get();
        } catch (BError e) {
            result = e;
        } catch (Throwable e) {
           throw ErrorCreator.createError(e);
        }
        if (completableFutures.length > 1 && result instanceof BError) {
            List<CompletableFuture<?>> nonErrorFutures = new ArrayList<>();
            for (CompletableFuture<?> completableFuture : completableFutures) {
                if (completableFuture.isDone()) {
                    try {
                        result = completableFuture.get();
                    } catch (Throwable e) {
                        throw ErrorCreator.createError(e);
                    }
                    if (!(result instanceof BError)) {
                        return result;
                    }
                } else {
                    nonErrorFutures.add(completableFuture);
                }
            }
            if (nonErrorFutures.isEmpty()) {
                return result;
            } else {
                return handleWaitAny(nonErrorFutures.toArray(new CompletableFuture<?>[0]));
            }
        }
        return result;
    }

    private AsyncUtils() {
    }
}
