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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlQName;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.XML_OPERATION_ERROR;

/**
 * Return attribute value matching attribute name `attrName`.
 *
 * @since 1.2.0
 */
public class GetAttribute {

    public static Object getAttribute(BXml xmlVal, BString attrName, boolean optionalFiledAccess) {
        if (xmlVal.getNodeType() == XmlNodeType.SEQUENCE && xmlVal.size() == 0) {
            return null;
        }
        if (!IsElement.isElement(xmlVal)) {
            return createError(XML_OPERATION_ERROR, BLangExceptionHelper.getErrorDetails(
                    RuntimeErrors.INVALID_XML_ATTRIBUTE_ERROR, xmlVal.getNodeType().value()));
        }
        BXmlQName qname = ValueCreator.createXmlQName(attrName);
        BString attrVal = xmlVal.getAttribute(qname.getLocalName(), qname.getUri());
        if (attrVal == null && !optionalFiledAccess) {
            return createError(XML_OPERATION_ERROR,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.ATTRIBUTE_NOT_FOUND_ERROR, attrName));
        }
        return attrVal;
    }

}
