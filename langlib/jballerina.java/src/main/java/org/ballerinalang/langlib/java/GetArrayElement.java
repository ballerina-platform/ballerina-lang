/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.java;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BHandle;

/**
 * This class contains the implementation of the "getArrayElement" ballerina function in
 * ballerina/jballerina.java module.
 *
 * @since 1.0.0
 */
public final class GetArrayElement {

    private GetArrayElement() {
    }

    public static BHandle getArrayElement(BHandle bHandle, long index) {
        Object[] arr = (Object[]) bHandle.getValue();
        if (arr == null) {
            throw JValues.getJavaNullReferenceError();
        }

        JValues.rangeCheck(index, arr);
        return ValueCreator.createHandleValue(arr[(int) index]);
    }
}
