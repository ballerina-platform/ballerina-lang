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

package org.ballerinalang.nativeimpl.lang.jsons;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Converts a JSON to the corresponding XML representation.
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "toXML",
        args = {@Argument(name = "j", type = TypeEnum.JSON),
                @Argument(name = "options", type = TypeEnum.STRUCT, structType = "Options",
                          structPackage = "ballerina.lang.jsons")},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts a JSON object to a XML representation") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "j",
        value = "A JSON object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "options",
        value = "Options for JSON to XML conversion ") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "xml",
        value = "XML value of the given JSON") })
public class ToXML extends AbstractJSONFunction {

    @Override
    public BValue[] execute(Context ctx) {
        BXML xml = null;
        try {
            // Accessing Parameters
            BJSON json = (BJSON) getRefArgument(ctx, 0);
            BStruct optionsStruct = ((BStruct) getRefArgument(ctx, 1));
            String attributePrefix = optionsStruct.getStringField(0);
            String arrayEntryTag = optionsStruct.getStringField(1);
            //Converting to xml.
            xml = JSONUtils.convertToXML(json, attributePrefix, arrayEntryTag);
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("convert json to xml", e);
        }
        return getBValues(xml);
    }
}
