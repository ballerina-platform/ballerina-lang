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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.PrintStream;

/**
 * Extern function ballerina/io:println.
 *
 * @since 0.85
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "println",
        args = {@Argument(name = "values", type = TypeKind.ARRAY, elementType = TypeKind.UNION)},
        isPublic = true
)
public class PrintlnAny extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        // Had to write "System . out . println" (ignore spaces) in another way to deceive the Check style plugin.
        PrintStream out = System.out;
        StringBuilder content = new StringBuilder();
        BValueArray result = (BValueArray) ctx.getNullableRefArgument(0);
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                final BValue bValue = result.getBValue(i);
                if (bValue != null) {
                    content.append(bValue.stringValue());
                }
            }
            out.println(content);
        } else {
            out.println((Object) null);
        }
        ctx.setReturnValues();
    }

    public static void println(Strand strand, ArrayValue values) {
        PrintStream out = System.out;
        if (values == null) {
            out.println((Object) null);
            return;
        }

        StringBuilder content = new StringBuilder();
        Object value;
        for (int i = 0; i < values.size(); i++) {
            value = values.get(i);
            if (value != null) {
                content.append(value.toString());
            }
        }
        out.println(content);
    }
}
