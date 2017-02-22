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
package org.ballerinalang.nativeimpl.lang.typemappers;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeTypeMapper;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaTypeMapper;
import org.ballerinalang.natives.annotations.ReturnType;


/**
 * Convert String to XML
 */
@BallerinaTypeMapper(
        packageName = "ballerina.lang.typemappers",
        typeMapperName = "stringToXML",
        args = {@Argument(name = "value", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts a string to XML") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "String value to be converted") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "xml",
        value = "XML representation of the given string") })
public class StringToXML extends AbstractNativeTypeMapper {

    public BValue convert(Context ctx) {
        BString msg = (BString) getArgument(ctx, 0);
        BXML result = new BXML(msg.stringValue());
        return result;
    }

}
