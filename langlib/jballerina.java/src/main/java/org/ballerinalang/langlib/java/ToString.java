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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BString;

/**
 * This class contains the implementation of the "toString" ballerina function in ballerina/jballerina.java module.
 *
 * @since 1.0.0
 */
public final class ToString {

    private ToString() {
    }

    public static Object toString(BHandle value) {
        Object referredValue = value.getValue();
        if (referredValue == null) {
            return null;
        }
        if (value instanceof BString) {
            return value;
        }
        return StringUtils.fromString(referredValue.toString());
    }

    public static BString toString(Long value) {
        return StringUtils.fromString(String.valueOf(value));
    }
}
