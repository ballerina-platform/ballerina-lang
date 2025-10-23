/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinates type initialization and annotation processing to ensure safe handling of
 * cross-references and cyclic dependencies.
 *
 * @since 2201.13.0
 */
public class TypeInitializer {

    @SuppressWarnings("unused")
    /*
     * Used for codegen for block on global lock while initializing the types
     */
    public static final ReentrantLock TYPE_INITIALIZING_GLOBAL_LOCK = new ReentrantLock();
    private static final Deque<Runnable> typeAnnotationFPs = new ArrayDeque<>();

    @SuppressWarnings("unused")
    /*
     * Used for codegen for load type annotations for annotated types
     */
    public static void loadTypeAnnotations(Runnable loadAnnotationFunc) {
        typeAnnotationFPs.add(loadAnnotationFunc);
        loadTypeAnnotations();
    }

    @SuppressWarnings("unused")
    /*
     * Used for codegen for load type annotations for non annotated types
     */
    public static void loadTypeAnnotations() {
        if (TYPE_INITIALIZING_GLOBAL_LOCK.getHoldCount() == 1) {
            while (!typeAnnotationFPs.isEmpty()) {
                typeAnnotationFPs.poll().run();
            }
        }
    }
}
