/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import javax.xml.namespace.QName;

/**
 * Change the name of element `xmlVal` to `newName`.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "setName",
//        args = {@Argument(name = "BXML", type = TypeKind.XML),
//                @Argument(name = "newName", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.NIL)},
//        isPublic = true
//)
public class SetName {
    private static final String OPERATION = "set element name in xml";


    public static void setName(BXml xmlVal, BString newNameBStr) {
        String newName = newNameBStr.getValue();
        if (!IsElement.isElement(xmlVal)) {
            throw ErrorHelper.getRuntimeException(ErrorCodes.XML_FUNC_TYPE_ERROR, "setName", "element");
        }

        try {
            if (xmlVal.getNodeType() == XmlNodeType.ELEMENT) {
                QName newQName;
                if (newName.startsWith("{")) {
                    int endCurly = newName.indexOf('}');
                    String nsUri = newName.substring(1, endCurly);
                    String localPart = newName.substring(endCurly + 1, newName.length());
                    newQName = new QName(nsUri, localPart);
                } else {
                    newQName = new QName(newName);
                }

                ((BXmlItem) xmlVal).setQName(newQName);
            }
        } catch (Throwable e) {
            ErrorHelper.handleXMLException(OPERATION, e);
        }
    }
}
