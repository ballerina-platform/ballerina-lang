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
package org.ballerinalang.toml.util;

/**
 * This class can be used to handle a singleton content with one element.
 *
 * @param <T> Any object
 * @since 0.964
 */
public class SingletonStack<T> {
    private T content;

    /**
     * Push object to the content.
     *
     * @param obj object to be pushed
     */
    public void push(T obj) {
        if (content != null) {
            throw new IllegalStateException("Stack is already full");
        }
        content = obj;
    }

    /**
     * Pops/Gets the object from content.
     *
     * @return object if exists, if content is null an exception
     */
    public T pop() {
        if (content == null) {
            throw new IllegalStateException("Stack is empty");
        }
        T lastKey = content;
        content = null;
        return lastKey;
    }

    /**
     * Check if the object exists in the content.
     *
     * @return if object exists true else false
     */
    public boolean present() {
        return content != null;
    }
}
