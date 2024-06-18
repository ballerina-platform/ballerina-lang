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

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;

/**
 * Helper function to check xml.isElement().
 */
public final class IsElement {

    private IsElement() {
    }

    public static boolean isElement(BXml bxml) {
        if (bxml.getNodeType() == XmlNodeType.ELEMENT) {
            return true;
        }
        if (bxml.getNodeType() == XmlNodeType.SEQUENCE) {
            return bxml.size() == 1
                    && ((BXmlSequence) bxml).getChildrenList().get(0).getNodeType() == XmlNodeType.ELEMENT;
        }
        return false;
    }
}
