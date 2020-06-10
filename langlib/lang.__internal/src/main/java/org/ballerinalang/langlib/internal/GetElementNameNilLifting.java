/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.internal;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.XML_OPERATION_ERROR;

/**
 * Return name of the element if `x` is a element or nil if element name is not set, else error.
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.__internal", version = "0.1.0",
        functionName = "getElementNameNilLifting",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class GetElementNameNilLifting {

    public static Object getElementNameNilLifting(Strand strand, XMLValue xmlVal, String attrName) {
        if (IsElement.isElement(xmlVal)) {
            String elementName = xmlVal.getElementName();
            if (elementName.equals("")) {
                return null;
            }
            return elementName;
        }
        String nodeTypeName = xmlVal.getNodeType().value();
        return createError(XML_OPERATION_ERROR, "XML " + nodeTypeName + " does not contain element name");
    }
}
