/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.langlib.decimal;

import io.ballerina.runtime.api.values.BDecimal;

/**
 * Native implementation of lang.decimal:min(decimal, decimal...).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.decimal", functionName = "min",
//        args = {@Argument(name = "n", type = TypeKind.DECIMAL), @Argument(name = "ns", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.DECIMAL)},
//        isPublic = true
//)
public class Min {

    public static BDecimal min(BDecimal n, BDecimal[] ns) {
        BDecimal min = n;
        int size = ns.length;
        for (int i = 0; i < size; i++) {
            BDecimal current = ns[i];
            min = current.value().compareTo(min.value()) <= 0 ? current : min;
        }
        return min;
    }
}
