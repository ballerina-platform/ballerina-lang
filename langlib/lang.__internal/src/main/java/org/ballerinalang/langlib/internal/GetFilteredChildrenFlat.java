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
import io.ballerina.runtime.api.values.BXMLItem;
import io.ballerina.runtime.api.values.BXMLSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Return children matching provided condition.
 * When operating on a xml sequence, flat map on each xml item in the sequence.
 *
 * @since 1.2.0
 */
public class GetFilteredChildrenFlat {

    public static BXML getFilteredChildrenFlat(BXML xmlVal, long index, BString[] elemNames) {
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            BXMLItem element = (BXMLItem) xmlVal;
            return ValueCreator.createXMLSequence(filterElementChildren(index, elemNames, element));
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            BXMLSequence sequence = (BXMLSequence) xmlVal;
            ArrayList<BXML> liftedFilteredChildren = new ArrayList<>();
            for (BXML child : sequence.getChildrenList()) {
                if (child.getNodeType() == XMLNodeType.ELEMENT) {
                    liftedFilteredChildren.addAll(filterElementChildren(index, elemNames, (BXMLItem) child));
                }
            }
            return ValueCreator.createXMLSequence(liftedFilteredChildren);

        }
        return ValueCreator.createXMLSequence();
    }

    private static List<BXML> filterElementChildren(long index, BString[] elemNames, BXMLItem element) {
        BXMLSequence elements = (BXMLSequence) GetElements.getElements(element.getChildrenSeq(), elemNames);
        if (index < 0) {
            // Return all elements
            return elements.getChildrenList();
        } else if (elements.getChildrenList().size() > index) {
            // Valid index; return requested element
            return Collections.singletonList((elements.getChildrenList().get((int) index)));
        } else {
            // OutOfRange return empty list
            return new ArrayList<>();
        }
    }
}
