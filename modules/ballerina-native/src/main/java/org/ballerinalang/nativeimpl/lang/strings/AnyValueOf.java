/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.lang.strings;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.model.strings:valueOf.
 *
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "valueOf",
        args = {@Argument(name = "value", type = TypeEnum.ANY)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a string representation of an integer argument") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "An any argument") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "String representation of the specified integer argument") })
public class AnyValueOf extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BValue value = getRefArgument(context, 0);
        if (value instanceof BJSON) {
            BString jsonStr = null;
            try {
                jsonStr = new BString(value.stringValue());
            } catch (Throwable e) {
                ErrorHandler.handleJsonException("get json as string", e);
            }
            return getBValues(jsonStr);
        } else if (value instanceof BXML) {
            BString xmlStr = null;
            try {
                xmlStr =  new BString(value.stringValue());
            } catch (Throwable e) {
                ErrorHandler.handleJsonException("get xml as string", e);
            }
            return getBValues(xmlStr);
        }
        return getBValues(new BString(value.stringValue()));
    }
}
