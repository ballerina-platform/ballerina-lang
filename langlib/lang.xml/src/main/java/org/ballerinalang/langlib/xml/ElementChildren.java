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
package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Return lift getChildren over sequences.
 *
 * @since 1.2
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "elementChildren",
//        args = {@Argument(name = "BXML", type = TypeKind.XML), @Argument(name = "nm", type = TypeKind.UNION)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public final class ElementChildren {

    private ElementChildren() {
    }

    public static BXml elementChildren(BXml xmlVal, Object nameObj) {
        boolean namedQuery = nameObj != null;
        String name = namedQuery ? ((BString) nameObj).getValue() : null;
        if (xmlVal.getNodeType() == XmlNodeType.ELEMENT) {
            if (namedQuery) {
                return (xmlVal).children().elements(name);
            }
            return (xmlVal).children().elements();
        } else if (xmlVal.getNodeType() == XmlNodeType.SEQUENCE) {
            List<BXml> items = new ArrayList<>();
            BXmlSequence sequence = (BXmlSequence) xmlVal.elements();
            for (BXml bxml : sequence.getChildrenList()) {
                if (bxml.getNodeType() != XmlNodeType.ELEMENT) {
                    continue;
                }
                for (BXml childElement : ((BXmlItem) bxml).getChildrenSeq().getChildrenList()) {
                    if (childElement.getNodeType() != XmlNodeType.ELEMENT) {
                        continue;
                    }
                    if (namedQuery) {
                        if (!childElement.getElementName().equals(name)) {
                            continue;
                        }
                    }
                    items.add(childElement);
                }
            }
            return ValueCreator.createXmlSequence(items);
        }
        return ValueCreator.createXmlSequence();
    }
}
