/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.launcher.toml.util;

/**
 * This class can be used to handle a singleton stack with one element
 *
 * @param <T>
 */
public class SingletonStack<T> {
    private T stack;

    /**
     * Constructor
     */
    public SingletonStack() {
        this.stack = (T) new Object[1];
    }

    /**
     * Push object to the stack
     *
     * @param obj
     */
    public void push(T obj) {
        stack = obj;
    }

    /**
     * Pops/Gets the object from stack
     *
     * @return object if exists, if stack is null an exception
     */
    public T pop() {
        if (stack == null) {
            throw new IllegalStateException("Current key is null");
        }
        T lastKey = stack;
        stack = null;
        return lastKey;
    }

    /**
     * Check if the object exists in the stack
     *
     * @return if object exists true else false
     */
    public boolean hasKey() {
        return stack != null;
    }
}
