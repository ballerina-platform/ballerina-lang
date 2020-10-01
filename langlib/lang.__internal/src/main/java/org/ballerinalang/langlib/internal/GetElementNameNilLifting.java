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

import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.values.XMLValue;

import static org.ballerinalang.jvm.api.BErrorCreator.createError;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.XML_OPERATION_ERROR;

/**
 * Return name of the element if `x` is a element or nil if element name is not set, else error.
 *
 * @since 1.2.0
 */
public class GetElementNameNilLifting {

    public static Object getElementNameNilLifting(XMLValue xmlVal) {
        if (IsElement.isElement(xmlVal)) {
            String elementName = xmlVal.getElementName();
            if (elementName.equals("")) {
                return null;
            }
            return elementName;
        }
        String nodeTypeName = xmlVal.getNodeType().value();
        return createError(XML_OPERATION_ERROR,
                           BStringUtils.fromString("XML " + nodeTypeName + " does not contain element name"));
    }
}
