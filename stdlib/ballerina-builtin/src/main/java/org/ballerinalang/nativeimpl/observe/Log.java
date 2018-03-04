/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.observe;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.observe.trace.OpenTracerBallerinaWrapper;

/**
 * This function adds logs to a span.
 */
@BallerinaFunction(
        packageName = "ballerina.observe",
        functionName = "log",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Span", structPackage = "ballerina.observe"),
        args = {@Argument(name = "logs", type = TypeKind.MAP)},
        isPublic = true
)
public class Log extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct span = (BStruct) getRefArgument(context, 0);
        String spanId = span.getStringField(0);
        BMap logs = (BMap) getRefArgument(context, 1);
        OpenTracerBallerinaWrapper.getInstance().log(spanId, Utils.toStringMap(logs));
        return VOID_RETURN;
    }
}
