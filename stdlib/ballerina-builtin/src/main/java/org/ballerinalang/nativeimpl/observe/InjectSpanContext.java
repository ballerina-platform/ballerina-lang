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

import org.ballerina.tracing.core.wrapper.OpenTracerBallerinaWrapper;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

/**
 * This function returns the span context of a given span.
 */
@BallerinaFunction(
        packageName = "ballerina.observe",
        functionName = "inject",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Span", structPackage = "ballerina.observe"),
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class InjectSpanContext extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct span = (BStruct) getRefArgument(context, 0);
        String spanId = span.getStringField(0);

        Map<String, String> propertiesToInject = OpenTracerBallerinaWrapper.getInstance().inject(spanId);

        BMap<String, BString> headerMap = new BMap<>();
        propertiesToInject.forEach((key, value) -> {
            headerMap.put(key, new BString(value));
        });
        return getBValues(headerMap);
    }
}
