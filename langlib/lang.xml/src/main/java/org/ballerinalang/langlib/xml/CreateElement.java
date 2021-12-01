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

import java.util.Map;

import javax.xml.XMLConstants;

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
    private static final String XML = "xml";
    private static final String XML_NS_URI_PREFIX = "{" + XMLConstants.XML_NS_URI + "}";
    private static final String XMLNS_NS_URI_PREFIX = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}";

    public static BXml createElement(BString name, BMap<BString, BString> attributes, BXml children) {
        String prefix = getPrefix(name.getValue(), attributes);
        BXmlQName xmlqName;
        if (prefix.equals("")) {
            xmlqName = ValueCreator.createXmlQName(name);
        } else {
            xmlqName = ValueCreator.createXmlQName(name, prefix);
        }
        String temp = null;
        BXml xmlElement = XmlFactory.createXMLElement(xmlqName, temp);
        xmlElement.setAttributes(attributes);
        xmlElement.setChildren(getChildren(children));
        return xmlElement;
    }

    private static String getPrefix(String name, BMap<BString, BString> attributes) {
        int curlyBracketEndIndex = name.lastIndexOf('}');
        if (name.startsWith("{") && curlyBracketEndIndex > 0) {
            String uri = name.substring(1, curlyBracketEndIndex);
            for (Map.Entry<BString, BString> entry : attributes.entrySet()) {
                if (entry.getValue().getValue().equals(uri)) {
                    String key = entry.getKey().getValue();
                    if (key.startsWith(XMLNS_NS_URI_PREFIX)) {
                        String prefix = key.substring(key.lastIndexOf('}') + 1);
                        if (prefix.equals(XML)) {
                            return "";
                        }
                        return prefix;
                    } else if (key.startsWith(XML_NS_URI_PREFIX)) {
                        // If `xml` namespace URI is used, we need to add `xml` namespace prefix to prefixMap
                        return XML;
                    }
                }
            }
        }
        return "";
    }

    private static BXml getChildren(BXml children) {
        if (children == null) {
            return ValueCreator.createXmlSequence();
        }
        return children;
    }
}

