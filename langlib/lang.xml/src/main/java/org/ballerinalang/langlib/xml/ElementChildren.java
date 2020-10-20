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

import io.ballerina.runtime.XMLNodeType;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLItem;
import io.ballerina.runtime.api.values.BXMLSequence;

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
public class ElementChildren {

    public static BXML elementChildren(BXML xmlVal, Object nameObj) {
        boolean namedQuery = nameObj != null;
        String name = namedQuery ? ((BString) nameObj).getValue() : null;
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            if (namedQuery) {
                return (xmlVal).children().elements(name);
            }
            return (xmlVal).children().elements();
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            List<BXML> items = new ArrayList<>();
            BXMLSequence sequence = (BXMLSequence) xmlVal.elements();
            for (BXML bxml : sequence.getChildrenList()) {
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    continue;
                }
                for (BXML childElement : ((BXMLItem) bxml).getChildrenSeq().getChildrenList()) {
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
            return ValueCreator.createXMLSequence(items);
        }
        return ValueCreator.createXMLSequence();
    }
}
