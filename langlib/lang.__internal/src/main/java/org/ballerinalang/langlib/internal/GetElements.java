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
import io.ballerina.runtime.api.values.BXmlSequence;

import java.util.ArrayList;

/**
 * Return elements matching at least one of `elemNames`.
 *
 * @since 1.2.0
 */
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
    public static BXml getElements(BXml xmlVal, BString[] elemNames) {

        ArrayList<String> nsList = new ArrayList<>();
        ArrayList<String> localNameList = new ArrayList<>();
        destructureFilters(elemNames, nsList, localNameList);

        // If this is a element; return this as soon as some filter match this elem. Else return empty sequence.
        if (IsElement.isElement(xmlVal)) {
            if (matchFilters(elemNames, nsList, localNameList, xmlVal.getElementName())) {
                return xmlVal;
            }
            return ValueCreator.createXmlSequence();
        }

        ArrayList<BXml> selectedElements = new ArrayList<>();
        if (xmlVal.getNodeType() == XmlNodeType.SEQUENCE) {
            BXmlSequence sequence = (BXmlSequence) xmlVal;
            for (BXml child : sequence.getChildrenList()) {
                if (child.getNodeType() != XmlNodeType.ELEMENT) {
                    continue;
                }
                if (matchFilters(elemNames, nsList, localNameList, child.getElementName())) {
                    selectedElements.add(child);
                }
            }
        }

        return ValueCreator.createXmlSequence(selectedElements);
    }

    public static void destructureFilters(BString[] elemNames,
                                          ArrayList<String> nsList, ArrayList<String> localNameList) {
        for (BString elemName : elemNames) {
            String fullName = elemName.getValue();
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
