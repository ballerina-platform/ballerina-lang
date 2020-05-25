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

import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import javax.xml.namespace.QName;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Change the name of element `xmlVal` to `newName`.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION,
        functionName = "setName",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML),
                @Argument(name = "newName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.NIL)},
        isPublic = true
)
public class SetName {
    private static final String OPERATION = "set element name in xml";


    public static void setName(Strand strand, XMLValue xmlVal, BString newNameBStr) {
        String newName = newNameBStr.getValue();
        if (!IsElement.isElement(strand, xmlVal)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "setName", "element");
        }

        try {
            if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
                QName newQName;
                if (newName.startsWith("{")) {
                    int endCurly = newName.indexOf('}');
                    String nsUri = newName.substring(0, endCurly);
                    String localPart = newName.substring(endCurly + 1, newName.length() - 1);
                    newQName = new QName(nsUri, localPart);
                } else {
                    newQName = new QName(newName);
                }

                ((XMLItem) xmlVal).setQName(newQName);
            }
        } catch (Throwable e) {
            BLangExceptionHelper.handleXMLException(OPERATION, e);
        }
    }
}
