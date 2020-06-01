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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaXMLSerializer;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.jvm.values.api.BXMLQName;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

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
public abstract class XMLValue implements RefValue, BXML, CollectionValue {

    BType type = BTypes.typeXML;

    public abstract int size();

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param attributeName Qualified name of the attribute
     * @return Value of the attribute
     */
    public BString getAttribute(BXMLQName attributeName) {
        return getAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix());
    }

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    @Deprecated
    public void setAttribute(BXMLQName attributeName, String value) {
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
    public void setAttribute(BXMLQName attributeName, BString value) {
        setAttributeOnInitialization(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix(),
                                     value.getValue());
    }

    /**
     * Get attributes as a {@link MapValueImpl}.
     * 
     * @return Attributes as a {@link MapValueImpl}
     */
    public abstract MapValue<BString, ?> getAttributesMap();

    /**
     * Set the attributes of the XML{@link MapValueImpl}.
     * 
     * @param attributes Attributes to be set.
     */
    public abstract void setAttributes(BMap<BString, ?> attributes);

    /**
     * Get the type of the XML.
     * 
     * @return Type of the XML
     */
    public abstract XMLNodeType getNodeType();

    /**
     * Builds itself.
     */
    public abstract void build();

    /**
     * {@inheritDoc}
     */
    @Override
    public BType getType() {
        return type;
    }

    protected abstract void setAttributesOnInitialization(BMap<BString, ?> attributes);

    protected abstract void setAttributeOnInitialization(String localName, String namespace, String prefix,
                                                         String value);

    // private methods

    protected static void handleXmlException(String message, Throwable t) {
        // Here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (t.getCause() != null) {
            throw new BallerinaException(message + t.getCause().getMessage());
        }

        throw new BallerinaException(message + t.getMessage());
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
    protected void addDescendants(List<BXML> descendants, XMLItem currentElement, List<String> qnames) {
        for (BXML child : currentElement.getChildrenSeq().children) {
            if (child.getNodeType() == XMLNodeType.ELEMENT) {
                String elemName = ((XMLItem) child).getQName().toString();
                if (qnames.contains(elemName)) {
                    descendants.add(child);
                }
                addDescendants(descendants, (XMLItem) child, qnames);
            }
        }
    }

    // TODO: These are bridge methods to invoke methods in BXML interface
    // Fix in the JVM code gen to directly call overridden BXML methods
    public void addChildren(XMLValue seq) {
        addChildren((BXML) seq);
    }

    public void setChildren(XMLValue seq) {
        setChildren((BXML) seq);
    }

    public abstract XMLValue children();

    public abstract XMLValue children(String qname);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XMLValue copy = (XMLValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    public abstract XMLValue getItem(int index);

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            if (outputStream instanceof BallerinaXMLSerializer) {
                ((BallerinaXMLSerializer) outputStream).write(this);
            } else {
                BallerinaXMLSerializer xmlSerializer = new BallerinaXMLSerializer(outputStream);
                xmlSerializer.write(this);
                xmlSerializer.flush();
                xmlSerializer.close();
            }
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }
}
