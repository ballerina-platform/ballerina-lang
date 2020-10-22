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

import io.ballerina.runtime.XMLNodeType;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLQName;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.XML_OPERATION_ERROR;

/**
 * Return attribute value matching attribute name `attrName`.
 *
 * @since 1.2.0
 */
public class GetAttribute {

    public static Object getAttribute(BXML xmlVal, BString attrName, boolean optionalFiledAccess) {
        if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE && xmlVal.size() == 0) {
            return null;
        }
        if (!IsElement.isElement(xmlVal)) {
            return createError(XML_OPERATION_ERROR,
                               StringUtils.fromString("Invalid xml attribute access on xml " +
                                                               xmlVal.getNodeType().value()));
        }
        BXMLQName qname = ValueCreator.createXMLQName(attrName);
        BString attrVal = xmlVal.getAttribute(qname.getLocalName(), qname.getUri());
        if (attrVal == null && !optionalFiledAccess) {
            return createError(XML_OPERATION_ERROR, StringUtils.fromString("attribute '" + attrName + "' not found"));
        }
        return attrVal;
    }

}
