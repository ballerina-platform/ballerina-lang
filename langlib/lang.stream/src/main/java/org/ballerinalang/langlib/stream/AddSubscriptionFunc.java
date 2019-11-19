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

package org.ballerinalang.langlib.stream;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native implementation of lang.stream.StreamManager:addSubscriptionFunc().
 *
 * @since 1.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream", functionName = "addSubscriptionFunc",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "StreamManager",
                structPackage = "ballerina/lang.stream"),
        args = {
                @Argument(name = "stream", type = TypeKind.STREAM),
                @Argument(name = "func", type = TypeKind.FUNCTION)
        },
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.FUNCTION)}
)
public class AddSubscriptionFunc {
    public static void addSubscriptionFunc(Strand strand, ObjectValue subscriptionMgr, StreamValue stream,
                                           FPValue<Object[], Object> subscriptionFunc) {
        ArrayValue funcArray = (ArrayValue) subscriptionMgr.getNativeData(stream.streamId);
        BFunctionType functionType = (BFunctionType) subscriptionFunc.getType();
        //TODO: This is a hack to make typeParam work
        functionType.paramTypes = new BType[] {BTypes.typePureType};
        if (funcArray == null) {
            funcArray = new ArrayValueImpl(new BArrayType(subscriptionFunc.getType()));
            subscriptionMgr.addNativeData(stream.streamId, funcArray);
        }

        funcArray.append(subscriptionFunc);
    }
}
