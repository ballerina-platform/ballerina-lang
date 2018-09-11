/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.runtime;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The registry which holds all the ballerina program exit hooks registered.
 *
 * @since 0.982.0
 */
public class BLangProgramExitHookRegistry {

    private static final Set<BLangProgramExitHook> exitHooks = new HashSet<>();

    /**
     * Add the given exit hook to the runtime.
     *
     * @param bLangProgramExitHook - exit hook to be added
     */
    public static void addExitHook(BLangProgramExitHook bLangProgramExitHook) {
        exitHooks.add(bLangProgramExitHook);
    }

    /**
     * Invoke all registered exit hooks.
     */
    public static void invokeExitHooks() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        exitHooks.forEach(hook -> {
            try {
                //Using a future here to allow timeout with method invocation
                CompletableFuture.supplyAsync(hook::invoke, executor).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                //ignore and continue
            }
        });
    }

    /**
     * Checks and return whether any hooks are added to the registry.
     *
     * @return true if the registry is empty, false otherwise
     */
    public static boolean isEmpty() {
        return exitHooks.isEmpty();
    }
}
