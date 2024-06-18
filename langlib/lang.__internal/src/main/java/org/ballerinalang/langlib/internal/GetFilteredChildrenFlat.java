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
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;

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

    private GetFilteredChildrenFlat() {
    }

    public static BXml getFilteredChildrenFlat(BXml xmlVal, long index, BString[] elemNames) {
        if (xmlVal.getNodeType() == XmlNodeType.ELEMENT) {
            BXmlItem element = (BXmlItem) xmlVal;
            return ValueCreator.createXmlSequence(filterElementChildren(index, elemNames, element));
        } else if (xmlVal.getNodeType() == XmlNodeType.SEQUENCE) {
            BXmlSequence sequence = (BXmlSequence) xmlVal;
            ArrayList<BXml> liftedFilteredChildren = new ArrayList<>();
            for (BXml child : sequence.getChildrenList()) {
                if (child.getNodeType() == XmlNodeType.ELEMENT) {
                    liftedFilteredChildren.addAll(filterElementChildren(index, elemNames, (BXmlItem) child));
                }
            }
            return ValueCreator.createXmlSequence(liftedFilteredChildren);

        }
        return ValueCreator.createXmlSequence();
    }

    private static List<BXml> filterElementChildren(long index, BString[] elemNames, BXmlItem element) {
        BXmlSequence elements = (BXmlSequence) GetElements.getElements(element.getChildrenSeq(), elemNames);
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
