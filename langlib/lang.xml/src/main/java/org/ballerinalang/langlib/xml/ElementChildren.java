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

import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.util.BLangCompilerConstants.XML_VERSION;

/**
 * Return lift getChildren over sequences.
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION,
        functionName = "elementChildren",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML), @Argument(name = "nm", type = TypeKind.UNION)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class ElementChildren {

    public static XMLValue elementChildren(Strand strand, XMLValue xmlVal, Object nameObj) {
        boolean namedQuery = nameObj != null;
        String name = namedQuery ? ((BString) nameObj).getValue() : null;
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            if (namedQuery) {
                return (XMLValue) ((XMLItem) xmlVal).children().elements(name);
            }
            return (XMLValue) ((XMLItem) xmlVal).children().elements();
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            List<BXML> items = new ArrayList<>();
            XMLSequence sequence = (XMLSequence) xmlVal.elements();
            for (BXML bxml : sequence.getChildrenList()) {
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    continue;
                }
                for (BXML childElement : ((XMLItem) bxml).getChildrenSeq().getChildrenList()) {
                    if (childElement.getNodeType() != XMLNodeType.ELEMENT) {
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
            return new XMLSequence(items);
        }
        return new XMLSequence();
    }
}
