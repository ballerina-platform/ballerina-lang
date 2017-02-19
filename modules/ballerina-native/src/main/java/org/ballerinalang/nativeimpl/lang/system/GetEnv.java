/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.model.system:getEnv.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "getEnv",
        args = {@Argument(name = "key", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the value of the specified environment variable.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "key",
        value = "The environment variable") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string)",
        value = "The value of the specified environment variable") })
public class GetEnv extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        String varName = getArgument(ctx, 0).stringValue();
        if (varName != null && varName.trim().length() > 0) {
            String varValue = System.getenv(varName);
            if (varValue != null && varValue.trim().length() > 0) {
                return getBValues(new BString(varValue));
            }

            return VOID_RETURN;
        }

        return VOID_RETURN;
    }
}
