/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.core;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;

/**
 * Type check function ballerina/test#getBallerinaType.
 */
public final class BallerinaTypeCheck {

    /**
     * Mark the constructor as private.
     */
    private BallerinaTypeCheck() {
    }

    /**
     * Get the Ballerina type of an object.
     * @param value Object
     * @return BString
     */
    public static BString getBallerinaType(Object value) {
        Type bType = TypeChecker.getType(value);
        String typeName = bType.getName();
        if (typeName == null) {
            typeName = "";
        }

        if (typeName.isEmpty()) {
            typeName = bType.toString();
        }

        return StringUtils.fromString(typeName);
    }
}
