/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.nativeimpl.lang.xmls;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Parse a string and get an XML.
 * 
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "parse",
        args = {@Argument(name = "xmlStr", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Parse string and get an XML") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "xmlStr",
        value = "String representation of XML") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "x",
        value = "Parsed xml") })
public class Parse extends AbstractNativeFunction {

private static final String OPERATION = "parse xml";

    @Override
    public BValue[] execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            String xmlStr = getStringArgument(ctx, 0);
            result = XMLUtils.parse(xmlStr);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }

        // Setting output value.
        return getBValues(result);
    }
}
