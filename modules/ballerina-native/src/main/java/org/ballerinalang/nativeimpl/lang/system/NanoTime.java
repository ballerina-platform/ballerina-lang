/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.model.system:nanoTime.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "nanoTime",
        returnType = {@ReturnType(type = TypeEnum.LONG)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the current system time in nanoseconds") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "long",
        value = "System time in nanoseconds") })
public class NanoTime extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        return getBValues(new BLong(System.nanoTime()));
    }
}
