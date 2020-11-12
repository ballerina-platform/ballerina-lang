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
 * Native implementation of lang.decimal:max(decimal, decimal...).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.decimal", functionName = "max",
//        args = {@Argument(name = "n", type = TypeKind.DECIMAL), @Argument(name = "ns", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.DECIMAL)},
//        isPublic = true
//)
public class Max {

    public static BDecimal max(BDecimal n, BDecimal[] ns) {
        BDecimal max = n;
        int size = ns.length;
        for (int i = 0; i < size; i++) {
            BDecimal current = ns[i];
            max = current.value().compareTo(max.value()) >= 0 ? current : max;
        }
        return max;
    }
}
