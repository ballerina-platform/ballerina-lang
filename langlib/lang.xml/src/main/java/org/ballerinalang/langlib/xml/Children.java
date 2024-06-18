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
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;

import java.util.ArrayList;

/**
 * Return lift getChildren over sequences.
 *
 * @since 1.2
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "children",
//        args = {@Argument(name = "BXML", type = TypeKind.XML)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class Children {

    private Children() {
    }

    public static BXml children(BXml xmlVal) {
        if (xmlVal.getNodeType() == XmlNodeType.ELEMENT) {
            return xmlVal.children();
        } else if (xmlVal.getNodeType() == XmlNodeType.SEQUENCE) {
            ArrayList<BXml> liftedChildren = new ArrayList<>();
            BXmlSequence sequence = (BXmlSequence) xmlVal.elements();
            for (BXml bxml : sequence.getChildrenList()) {
                liftedChildren.addAll(((BXmlItem) bxml).getChildrenSeq().getChildrenList());
            }
            return ValueCreator.createXmlSequence(liftedChildren);
        }
        return ValueCreator.createXmlSequence();
    }
}
