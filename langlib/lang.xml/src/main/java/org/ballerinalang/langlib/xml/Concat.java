/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
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
 * Concatenate xml items into a new sequence. Empty xml sequence if empty.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml", version = XML_VERSION,
        functionName = "concat",
        args = {@Argument(name = "arrayValue", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class Concat {

    public static XMLValue concat(Strand strand, ArrayValue arrayValue) {
        List<BXML> backingArray = new ArrayList<>();
        XMLValue lastItem = null;
        for (int i = 0; i < arrayValue.size(); i++) {
            Object refValue = arrayValue.getRefValue(i);
            if (refValue instanceof BString) {
                if (lastItem != null && lastItem.getNodeType() == XMLNodeType.TEXT) {
                    // If last added item is a string, then concat prev values with this values and replace prev value.
                    String concat = lastItem.getTextValue() + refValue;
                    XMLValue xmlText = XMLFactory.createXMLText(concat);
                    backingArray.set(backingArray.size() - 1, xmlText);
                    lastItem = xmlText;
                    continue;
                }
                XMLValue xmlText = XMLFactory.createXMLText((BString) refValue);
                backingArray.add(xmlText);
                lastItem = xmlText;
            } else {
                backingArray.add((XMLValue) refValue);
                lastItem = (XMLValue) refValue;
            }
        }
        return new XMLSequence(backingArray);
    }
}
