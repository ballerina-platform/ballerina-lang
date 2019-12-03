/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.PrintStream;


/**
 * Extern function print.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "print",
        args = {@Argument(name = "values", type = TypeKind.ARRAY, elementType = TypeKind.UNION)},
        isPublic = true
)
public class PrintAny {

    public static void print(Strand strand, ArrayValue values) {
        PrintStream out = System.out;
        if (values == null) {
            out.print((Object) null);
            return;
        }

        Object value;
        for (int i = 0; i < values.size(); i++) {
            value = values.get(i);
            if (value != null) {
                out.print(StringUtils.getStringValue(value));
            }
        }
    }
}
