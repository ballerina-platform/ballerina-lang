/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.model.values;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;


/**
 * {@code BXML} represents a XML value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BXML extends BallerinaMessageDataSource implements BRefType<OMElement> {

    private OMElement value;
    private OutputStream outputStream;

    /**
     * Initialize a {@link BXML} from a XML string.
     *
     * @param xmlValue A XML string
     */
    public BXML(String xmlValue) {
        if (xmlValue != null) {
            try {
                value = AXIOMUtil.stringToOM(xmlValue);
            } catch (XMLStreamException e) {
                throw new BallerinaException("Cannot create OMElement from given String, maybe malformed String: " +
                        e.getMessage());
            }
        }
    }

    /**
     * Initialize a {@link BXML} from a {@link org.apache.axiom.om.OMElement} object.
     *
     * @param value xml object
     */
    public BXML(OMElement value) {
        this.value = value;
    }

    /**
     * Create a {@link BXML} from a {@link InputStream}.
     *
     * @param inputStream Input Stream
     */
    public BXML(InputStream inputStream) {
        if (inputStream != null) {
            try {
                value = new StAXOMBuilder(inputStream).getDocumentElement();
            } catch (XMLStreamException e) {
                throw new BallerinaException("Cannot create OMElement from given source: " + e.getMessage());
            }
        }
    }

    /**
     * Create an empty XMLValue.
     */
    public BXML() {
        // do nothing
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void serializeData() {
        try {
            this.value.serialize(this.outputStream);
        } catch (XMLStreamException e) {
            throw new BallerinaException("Error occurred during writing the message to the output stream", e);
        }
    }

    @Override
    public OMElement value() {
        return this.value;
    }

    @Override
    public String stringValue() {
        if (this.value != null) {
            return this.value.toString();
        }

        return "";
    }

    @Override
    public String getMessageAsString() {
        if (this.value != null) {
            return this.value.toString();
        }
        return "";
    }
}
