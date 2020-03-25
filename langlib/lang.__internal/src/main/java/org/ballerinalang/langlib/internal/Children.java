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
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;

/**
 * Return lift getChildren over sequences.
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.__internal",
        functionName = "children",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class Children {

    public static XMLValue children(Strand strand, XMLValue xmlVal) {
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            return ((XMLItem) xmlVal).children();
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            ArrayList<BXML> liftedChildren = new ArrayList<>();
            XMLSequence sequence = (XMLSequence) xmlVal.elements();
            for (BXML bxml : sequence.getChildrenList()) {
                liftedChildren.addAll(((XMLItem) bxml).getChildrenSeq().getChildrenList());
            }
            return new XMLSequence(liftedChildren);
        }
        return new XMLSequence();
    }
}
