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
package org.ballerinalang.nativeimpl.lang.xmls;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStruct;
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
 * Converts a XML to the corresponding JSON representation.
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "toJSON",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "options", type = TypeEnum.STRUCT, structType = "Options",
                          structPackage = "ballerina.lang.xmls")},
        returnType = {@ReturnType(type = TypeEnum.JSON)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts a XML object to a JSON representation") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "A XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "options",
        value = "Options for xml to json conversion ") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "json",
        value = "JSON value of the given XML") })
public class ToJSON extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context ctx) {
        BJSON json = null;
        try {
            // Accessing Parameters
            BXML xml = (BXML) getRefArgument(ctx, 0);
            BStruct optionsStruct = ((BStruct) getRefArgument(ctx, 1));
            String attributePrefix = optionsStruct.getStringField(0);
            Boolean preserveNamespaces = optionsStruct.getBooleanField(0) == 1;
            //Converting to json
            json = XMLUtils.convertToJSON(xml, attributePrefix, preserveNamespaces);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException("convert xml to json", e);
        }

        return getBValues(json);
    }
}

