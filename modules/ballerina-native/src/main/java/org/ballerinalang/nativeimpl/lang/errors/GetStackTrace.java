/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.lang.errors;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native functions for ballerina.model.exceptions to Set the message and the category.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.errors",
        functionName = "getStackTrace",
        args = {@Argument(name = "err", type = TypeEnum.STRUCT, structType = "Error",
                structPackage = "ballerina.lang.errors")},
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "StackTrace",
                structPackage = "ballerina.lang.errors")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the exception stack trace.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "err",
        value = "The error struct")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "struct",
        value = "The stackTrace struct")})
public class GetStackTrace extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct value = ((BStruct) getRefArgument(context, 0)).getStackTrace();
        if (value == null) {
            return new BValue[]{};
        }
        return new BValue[]{value};
    }
}
