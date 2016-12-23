/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.values;

import java.lang.reflect.Array;

/**
 * {@code BArray} represents an array value in Ballerina
 *
 * @param <T> Ballerina value stored in this array value
 * @since 1.0.0
 */
public class BArray<T extends BValue> implements BRefType {

    private T[] tArray;

    public BArray(T[] tArray) {
        this.tArray = tArray;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public T value() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BValue> T[] createArray(Class<T[]> tClass) {

        return (T[]) Array.newInstance(tClass.getComponentType(), 100);
    }
}
