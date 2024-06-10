/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlQName;
import io.ballerina.runtime.internal.BallerinaXmlSerializer;
import io.ballerina.runtime.internal.IteratorUtils;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import static io.ballerina.runtime.internal.ValueUtils.getTypedescValue;

/**
 * {@code BXML} represents an XML in Ballerina. An XML could be one of:
 * <ul>
 * <li>element</li>
 * <li>text</li>
 * <li>comment</li>
 * <li>processing instruction</li>
 * <li>sequence of above</li>
 * </ul>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 0.995.0
 */
public abstract class XmlValue implements RefValue, BXml, CollectionValue {

    Type type = PredefinedTypes.TYPE_XML;
    protected BTypedesc typedesc;

    protected Type iteratorNextReturnType;

    @Override
    public abstract int size();

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param attributeName Qualified name of the attribute
     * @return Value of the attribute
     */
    @Override
    public BString getAttribute(BXmlQName attributeName) {
        return getAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix());
    }

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    @Override
    @Deprecated
    public void setAttribute(BXmlQName attributeName, String value) {
        setAttributeOnInitialization(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix(),
                                     value);
    }

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     *
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    @Deprecated
    public void setAttribute(BXmlQName attributeName, BString value) {
        setAttributeOnInitialization(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix(),
                                     value.getValue());
    }

    /**
     * Get attributes as a {@link MapValueImpl}.
     * 
     * @return Attributes as a {@link MapValueImpl}
     */
    @Override
    public abstract MapValue<BString, BString> getAttributesMap();

    /**
     * Set the attributes of the XML{@link MapValueImpl}.
     * 
     * @param attributes Attributes to be set.
     */
    @Override
    public abstract void setAttributes(BMap<BString, BString> attributes);

    /**
     * Get the type of the XML.
     * 
     * @return Type of the XML
     */
    @Override
    public abstract XmlNodeType getNodeType();

    /**
     * Builds itself.
     */
    @Override
    public abstract void build();

    @Override
    public String informalStringValue(BLink parent) {
        return "`" + stringValue(parent) + "`";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType() {
        return type;
    }

    protected abstract void setAttributesOnInitialization(BMap<BString, BString> attributes);

    protected abstract void setAttributeOnInitialization(String localName, String namespace, String prefix,
                                                         String value);

    // private methods

    protected static void handleXmlException(String message, Throwable t) {
        // Here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (t.getCause() != null) {
            throw ErrorCreator.createError(StringUtils.fromString(message + t.getCause().getMessage()));
        }

        throw ErrorCreator.createError(StringUtils.fromString(message + t.getMessage()));
    }

    /**
     * Get the {@link QName} from {@link String}.
     *
     * @param qname String representation of qname
     * @return constructed {@link QName}
     */
    protected QName getQname(String qname) {
        String nsUri;
        String localname;
        int rParenIndex = qname.indexOf('}');

        if (qname.startsWith("{") && rParenIndex > 0) {
            localname = qname.substring(rParenIndex + 1);
            nsUri = qname.substring(1, rParenIndex);
        } else {
            localname = qname;
            nsUri = "";
        }

        return new QName(nsUri, localname);
    }

    /**
     * Recursively traverse and add the descendant with the given name to the descendants list.
     * @param descendants List to add descendants
     * @param currentElement Current node
     * @param qnames Qualified names of the descendants to search
     */
    protected void addDescendants(List<BXml> descendants, XmlItem currentElement, List<String> qnames) {
        for (BXml child : currentElement.getChildrenSeq().getChildrenList()) {
            if (child.getNodeType() == XmlNodeType.ELEMENT) {
                String elemName = ((XmlItem) child).getQName().toString();
                if (qnames.contains(elemName)) {
                    descendants.add(child);
                }
                addDescendants(descendants, (XmlItem) child, qnames);
            }
        }
    }

    protected void addDescendants(List<BXml> descendants, XmlItem currentElement) {
        for (BXml child : currentElement.getChildrenSeq().getChildrenList()) {
            descendants.add(child);
            if (child.getNodeType() == XmlNodeType.ELEMENT) {
                addDescendants(descendants, (XmlItem) child);
            }
        }
    }

    // TODO: These are bridge methods to invoke methods in BXML interface
    // Fix in the JVM code gen to directly call overridden BXML methods
    public void addChildren(XmlValue seq) {
        addChildren((BXml) seq);
    }

    public void setChildren(XmlValue seq) {
        setChildren((BXml) seq);
    }

    @Override
    public abstract XmlValue children();

    @Override
    public abstract XmlValue children(String qname);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XmlValue copy = (XmlValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    @Override
    public abstract XmlValue getItem(int index);

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            if (outputStream instanceof BallerinaXmlSerializer xmlSerializer) {
                xmlSerializer.write(this);
            } else {
                BallerinaXmlSerializer xmlSerializer = new BallerinaXmlSerializer(outputStream);
                xmlSerializer.write(this);
                xmlSerializer.flush();
                xmlSerializer.close();
            }
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }

    @Override
    public BTypedesc getTypedesc() {
        if (this.typedesc == null) {
            this.typedesc = getTypedescValue(type, this);
        }
        return typedesc;
    }

    @Override
    public Type getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(this.type);
        }
        return iteratorNextReturnType;
    }

}
