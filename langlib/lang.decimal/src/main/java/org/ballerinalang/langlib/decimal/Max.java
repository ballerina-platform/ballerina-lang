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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.util.BLangCompilerConstants.DECIMAL_VERSION;

/**
 * Native implementation of lang.decimal:max(decimal, decimal...).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.decimal", version = DECIMAL_VERSION, functionName = "max",
        args = {@Argument(name = "n", type = TypeKind.DECIMAL), @Argument(name = "ns", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.DECIMAL)},
        isPublic = true
)
public class Max {

    public static DecimalValue max(Strand strand, DecimalValue n, ArrayValue ns) {
        DecimalValue max = n;
        int size = ns.size();
        for (int i = 0; i < size; i++) {
            DecimalValue current = (DecimalValue) ns.get(i);
            max = current.value().compareTo(max.value()) >= 0 ? current : max;
        }
        return max;
    }
}
