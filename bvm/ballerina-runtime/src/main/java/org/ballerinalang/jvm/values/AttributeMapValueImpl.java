/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;

import javax.xml.XMLConstants;

import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;
import static org.ballerinalang.jvm.values.XMLItem.XMLNS_URL_PREFIX;

/**
 *  Validating xml attribute map.
 *
 * @since 1.3
 */
class AttributeMapValueImpl extends MapValueImpl<String, String> {

    public AttributeMapValueImpl() {
        super(new BMapType(BTypes.typeString));
    }

    @Override
    public String put(String key, String value) {
        synchronized (this) {
            if (super.isFrozen()) {
                FreezeUtils.handleInvalidUpdate(State.FROZEN, XML_LANG_LIB);
            }
        }

        String localName = "";
        String namespaceUri = "";
        int closingCurlyPos = key.lastIndexOf('}');
        if (closingCurlyPos == -1) { // no '}' found
            localName = key;
        } else {
            namespaceUri = key.substring(1, closingCurlyPos);
            localName = key.substring(closingCurlyPos + 1);
        }

        if (localName.isEmpty()) {
            throw BallerinaErrors.createError("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);

        if (namespaceUri.isEmpty()) {
            return super.put(localName, value);
        }
        return super.put(key, value);
    }

    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {

        if (localName == null || localName.isEmpty()) {
            throw BallerinaErrors.createError("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);
        XMLValidator.validateXMLName(prefix);

        // JVM codegen uses prefix == 'xmlns' and namespaceUri == null to denote namespace decl at runtime.
        // 'localName' will contain the namespace name where as 'value' will contain the namespace URI
        // todo: Fix this so that namespaceURI points to XMLConstants.XMLNS_ATTRIBUTE_NS_URI
        //  and remove this special case
        if ((namespaceUri == null && prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
                || localName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            String nsNameDecl = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + localName;
            super.put(nsNameDecl, value);
            return;
        }

        String nsOfPrefix = get(XMLNS_URL_PREFIX + prefix);
        if (namespaceUri != null && nsOfPrefix != null && !namespaceUri.equals(nsOfPrefix)) {
            String errorMsg = String.format(
                    "failed to add attribute '%s:%s'. prefix '%s' is already bound to namespace '%s'",
                    prefix, localName, prefix, nsOfPrefix);
            throw BallerinaErrors.createError(errorMsg);
        }

        if ((namespaceUri == null || namespaceUri.isEmpty())) {
            super.put(localName, value);
        } else {
            // If the attribute already exists, update the value.
            super.put("{" + namespaceUri + "}" + localName, value);
        }

        // If the prefix is 'xmlns' then this is a namespace addition
        if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            String xmlnsPrefix = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + prefix;
            super.put(xmlnsPrefix, namespaceUri);
        }
    }
}
