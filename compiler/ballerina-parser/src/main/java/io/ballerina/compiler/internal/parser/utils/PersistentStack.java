/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.utils;

/**
 * A persistent/immutable stack implementation.
 *
 * @param <T> The type of the element
 * @since 1.3.0
 */
public class PersistentStack<T> {

    private static final PersistentStack<?> EMPTY = new PersistentStack<>(null, null);
    private final T head;
    private final PersistentStack<T> tail;

    private PersistentStack(T head, PersistentStack<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    @SuppressWarnings("unchecked")
    public static <T> PersistentStack<T> getEmpty() {
        return (PersistentStack<T>) EMPTY;
    }

    public PersistentStack<T> pop() {
        return tail;
    }

    public PersistentStack<T> push(T head) {
        return new PersistentStack<>(head, this);
    }

    public T peek() {
        return head;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }
}
