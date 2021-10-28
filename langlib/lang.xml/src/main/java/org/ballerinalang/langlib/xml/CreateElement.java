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
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlQName;
import io.ballerina.runtime.internal.XmlFactory;

/**
 * Create XML element from tag name and children sequence.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "createElement",
//        args = {
//                @Argument(name = "name", type = TypeKind.STRING),
//                @Argument(name = "children", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.XML)},
//        isPublic = true
//)
public class CreateElement {

    public static BXml createElement(BString name, BMap<BString, BString> attributes, BXml children) {
        BXmlQName xmlqName = ValueCreator.createXmlQName(name);
        String temp = null;
        BXml xmlElement = XmlFactory.createXMLElement(xmlqName, temp);
        xmlElement.setAttributes(attributes);
        xmlElement.setChildren(getChildren(children));
        return xmlElement;
    }

    private static BXml getChildren(BXml children) {
        if (children == null) {
            return ValueCreator.createXmlSequence();
        }
        return children;
    }
}

