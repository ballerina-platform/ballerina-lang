/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.lang.system:sleep.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "sleep",
        args = {@Argument(name = "t", type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sleep the running thread for the specified time period")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "t",
        value = "sleep time in milliseconds")})
public class Sleep extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        long timeout = getIntArgument(ctx, 0);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new BallerinaException("System sleep has been interrupted", e);
        }
        return VOID_RETURN;
    }
}
