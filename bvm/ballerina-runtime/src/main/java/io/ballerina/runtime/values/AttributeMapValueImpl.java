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
package io.ballerina.runtime.values;

import io.ballerina.runtime.XMLValidator;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;

import javax.xml.XMLConstants;

import static io.ballerina.runtime.util.BLangConstants.XML_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;
import static io.ballerina.runtime.values.XMLItem.XMLNS_URL_PREFIX;

/**
 * Validating xml attribute map.
 *
 * @since 1.3
 */
class AttributeMapValueImpl extends MapValueImpl<BString, BString> {

    public AttributeMapValueImpl() {
        super(new BMapType(PredefinedTypes.TYPE_STRING));
    }

    public AttributeMapValueImpl(boolean readonly) {
        super(new BMapType(PredefinedTypes.TYPE_STRING));

        if (readonly) {
            this.freezeDirect();
        }
    }

    @Override
    public BString put(BString keyBStr, BString value) {
        if (isFrozen()) {
            throw ErrorCreator.createError(getModulePrefixedReason(XML_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                                           BLangExceptionHelper.getErrorMessage(INVALID_READONLY_VALUE_UPDATE));
        }

        return insertValue(keyBStr, value, false);
    }

    @Override
    public void populateInitialValue(BString key, BString value) {
        insertValue(key, value, true);
    }

    void setAttribute(String localName, String namespaceUri, String prefix, String value, boolean onInitialization) {
        PutAttributeFunction func = onInitialization ? super::populateInitialValue : super:: put;

        if (localName == null || localName.isEmpty()) {
            throw ErrorCreator.createError(StringUtils.fromString(("localname of the attribute cannot be empty")));
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
            func.put(StringUtils.fromString(nsNameDecl), StringUtils.fromString(value));
            return;
        }

        BString nsOfPrefix = get(StringUtils.fromString(XMLNS_URL_PREFIX + prefix));
        if (namespaceUri != null && nsOfPrefix != null && !namespaceUri.equals(nsOfPrefix.getValue())) {
            String errorMsg = String.format(
                    "failed to add attribute '%s:%s'. prefix '%s' is already bound to namespace '%s'",
                    prefix, localName, prefix, nsOfPrefix.getValue());
            throw ErrorCreator.createError(StringUtils.fromString((errorMsg)));
        }

        if ((namespaceUri == null || namespaceUri.isEmpty())) {
            func.put(StringUtils.fromString(localName), StringUtils.fromString(value));
        } else {
            // If the attribute already exists, update the value.
            func.put(StringUtils.fromString("{" + namespaceUri + "}" + localName), StringUtils.fromString(value));
        }

        // If the prefix is 'xmlns' then this is a namespace addition
        if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            String xmlnsPrefix = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + prefix;
            func.put(StringUtils.fromString(xmlnsPrefix), StringUtils.fromString(namespaceUri));
        }
    }

    private BString insertValue(BString keyBStr, BString value, boolean onInitialization) {
        String key = keyBStr.getValue();
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
            throw ErrorCreator.createError(StringUtils.fromString(("localname of the attribute cannot be empty")));
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);

        BString keyToInsert = namespaceUri.isEmpty() ? StringUtils.fromString(localName) : keyBStr;

        if (!onInitialization) {
            return super.put(keyToInsert, value);
        }

        super.populateInitialValue(keyToInsert, value);
        return null;
    }

    private interface PutAttributeFunction {
        void put(BString key, BString value);
    }
}
