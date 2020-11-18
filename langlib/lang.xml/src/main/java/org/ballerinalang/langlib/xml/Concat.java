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

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.XMLNodeType;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;

import java.util.ArrayList;
import java.util.List;

/**
 * Concatenate xml items into a new sequence. Empty xml sequence if empty.
 *
 * @since 1.0
 */
public class Concat {

    public static BXML concat(Object... arrayValue) {
        List<BXML> backingArray = new ArrayList<>();
        BXML lastItem = null;
        for (int i = 0; i < arrayValue.length; i++) {
            Object refValue = arrayValue[i];
            if (refValue instanceof BString) {
                if (lastItem != null && lastItem.getNodeType() == XMLNodeType.TEXT) {
                    // If last added item is a string, then concat prev values with this values and replace prev value.
                    String concat = lastItem.getTextValue() + refValue;
                    BXML xmlText = XMLFactory.createXMLText(concat);
                    backingArray.set(backingArray.size() - 1, xmlText);
                    lastItem = xmlText;
                    continue;
                }
                BXML xmlText = XMLFactory.createXMLText((BString) refValue);
                backingArray.add(xmlText);
                lastItem = xmlText;
            } else {
                backingArray.add((BXML) refValue);
                lastItem = (BXML) refValue;
            }
        }
        return ValueCreator.createXMLSequence(backingArray);
    }
}
