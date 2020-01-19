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

import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.jvm.values.api.BXMLQName;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.List;

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

    protected volatile Status freezeStatus = new Status(State.UNFROZEN);

    public abstract int size();

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param attributeName Qualified name of the attribute
     * @return Value of the attribute
     */
    public String getAttribute(BXMLQName attributeName) {
        return getAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix());
    }

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    public void setAttribute(BXMLQName attributeName, String value) {
        setAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix(), value);
    }

    @Override
    public MapValue<String, ?> getAttributesMap() {
        return new MapValueImpl<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BType getType() {
        return type;
    }

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
            localname = qname.substring(rParenIndex + 1, qname.length());
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
     * @param qname Qualified name of the descendants to search
     */
    protected void addDescendants(List<BXML> descendants, XMLItem currentElement, String qname) {
        for (BXML child : currentElement.getChildrenSeq().children) {
            if (child.getNodeType() == XMLNodeType.ELEMENT) {
                if (((XMLItem) child).getQName().toString().equals(qname)) {
                    descendants.add(child);
                    continue;
                } else {
                    addDescendants(descendants, (XMLItem) child, qname);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
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

    public abstract XMLValue getItem(int index);
}
