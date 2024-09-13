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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.XmlFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Concatenate xml items into a new sequence. Empty xml sequence if empty.
 *
 * @since 1.0
 */
public class Concat {

    public static BXml concat(Object... arrayValue) {
        List<BXml> backingArray = new ArrayList<>();
        BXml lastItem = null;
        for (Object refValue : arrayValue) {
            if (refValue instanceof BString bString) {
                if (lastItem != null && lastItem.getNodeType() == XmlNodeType.TEXT) {
                    // If last added item is a string, then concat prev values with this values and replace prev value.
                    String concat = lastItem.getTextValue() + refValue;
                    BXml xmlText = XmlFactory.createXMLText(concat);
                    backingArray.set(backingArray.size() - 1, xmlText);
                    lastItem = xmlText;
                    continue;
                }
                BXml xmlText = XmlFactory.createXMLText(bString);
                backingArray.add(xmlText);
                lastItem = xmlText;
            } else if (refValue instanceof BXmlSequence bXmlSequence) {
                backingArray.addAll(bXmlSequence.getChildrenList());
                lastItem = (BXml) refValue;
            } else {
                backingArray.add((BXml) refValue);
                lastItem = (BXml) refValue;
            }
        }
        return ValueCreator.createXmlSequence(backingArray);
    }
}
