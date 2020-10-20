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
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLSequence;

import java.util.ArrayList;

/**
 * Return elements matching at least one of `elemNames`.
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.__internal",
//        functionName = "getElements",
//        args = {@Argument(name = "BXML", type = TypeKind.XML),
//                @Argument(name = "elemNames", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class GetElements {


    public static final String EMPTY = "";
    public static final String STAR = "*";

    /**
     * Expected element name format.
     * elemNames: {nsUrl}elemName | elemName | {nsUrl}* | *
     *
     * @param xmlVal the XML value
     * @param elemNames element names to select
     * @return sequence of elements matching given element names
     */
    public static BXML getElements(BXML xmlVal, BString[] elemNames) {

        ArrayList<String> nsList = new ArrayList<>();
        ArrayList<String> localNameList = new ArrayList<>();
        destructureFilters(elemNames, nsList, localNameList);

        // If this is a element; return this as soon as some filter match this elem. Else return empty sequence.
        if (IsElement.isElement(xmlVal)) {
            if (matchFilters(elemNames, nsList, localNameList, xmlVal.getElementName())) {
                return xmlVal;
            }
            return ValueCreator.createXMLSequence();
        }

        ArrayList<BXML> selectedElements = new ArrayList<>();
        if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            BXMLSequence sequence = (BXMLSequence) xmlVal;
            for (BXML child : sequence.getChildrenList()) {
                if (child.getNodeType() != XMLNodeType.ELEMENT) {
                    continue;
                }
                if (matchFilters(elemNames, nsList, localNameList, child.getElementName())) {
                    selectedElements.add(child);
                }
            }
        }

        return ValueCreator.createXMLSequence(selectedElements);
    }

    public static void destructureFilters(BString[] elemNames,
                                          ArrayList<String> nsList, ArrayList<String> localNameList) {
        int filterCount = elemNames.length;
        for (int i = 0; i < filterCount; i++) {
            String fullName = elemNames[i].getValue();
            int lastIndexOf = fullName.lastIndexOf('}');
            if (lastIndexOf < 0) {
                nsList.add(EMPTY);
                localNameList.add(fullName);
            } else {
                nsList.add(fullName.substring(1, lastIndexOf));
                localNameList.add(fullName.substring(lastIndexOf + 1));
            }
        }
    }

    public static boolean matchFilters(BString[] elemNames,
                                        ArrayList<String> nsList, ArrayList<String> elemList, String elementName) {
        int filterCount = elemNames.length;
        for (int i = 0; i < filterCount; i++) {
            String ns = nsList.get(i);
            String eName = elemList.get(i);
            // .<*>
            if (ns.equals(EMPTY) && eName.equals(STAR)) {
                return true;
            }
            // .<ns:*>
            if (eName.equals(STAR)) {
                int index = elementName.lastIndexOf('}');
                if (index > 0 && elementName.substring(1, index).equals(ns)) {
                    return true;
                }
            }
            // .<ns:foo> or .<foo>
            if (elementName.equals(elemNames[i].getValue())) {
                return true;
            }
        }
        return false;
    }
}
