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

import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Return children matching provided condition.
 * When operating on a xml sequence, flat map on each xml item in the sequence.
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.__internal", version = "0.1.0",
        functionName = "getFilteredChildrenFlat",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML),
                @Argument(name = "index", type = TypeKind.INT),
                @Argument(name = "elemNames", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class GetFilteredChildrenFlat {


    public static XMLValue getFilteredChildrenFlat(Strand strand, XMLValue xmlVal, long index, ArrayValue elemNames) {
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            XMLItem element = (XMLItem) xmlVal;
            XMLSequence xmlSeq = new XMLSequence(filterElementChildren(strand, index, elemNames, element));
            return xmlSeq;
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            XMLSequence sequence = (XMLSequence) xmlVal;
            ArrayList<BXML> liftedFilteredChildren = new ArrayList<>();
            for (BXML child : sequence.getChildrenList()) {
                if (child.getNodeType() == XMLNodeType.ELEMENT) {
                    liftedFilteredChildren.addAll(filterElementChildren(strand, index, elemNames, (XMLItem) child));
                }
            }
            return new XMLSequence(liftedFilteredChildren);

        }
        return new XMLSequence();
    }

    private static List<BXML> filterElementChildren(Strand strand, long index, ArrayValue elemNames, XMLItem element) {
        XMLSequence elements = (XMLSequence) GetElements.getElements(strand, element.getChildrenSeq(), elemNames);
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
