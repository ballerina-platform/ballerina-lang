/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.values;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.io.OutputStream;

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
            } catch (Throwable t) {
                handleXmlException("failed to create xml: ", t);
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
            } catch (Throwable t) {
                handleXmlException("failed to create xml: ", t);
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
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }

    @Override
    public OMElement value() {
        return this.value;
    }

    @Override
    public String stringValue() {
        if (this.value != null) {
            try {
                return this.value.toString();
            } catch (Throwable t) {
                handleXmlException("failed to get xml as string: ", t);
            }
        }

        return "";
    }

    @Override
    public String getMessageAsString() {
        if (this.value != null) {
            try {
                return this.value.toString();
            } catch (Throwable t) {
                handleXmlException("failed to get xml as string: ", t);
            }
        }
        return "";
    }
    
    private static void handleXmlException(String message, Throwable t) {
        // Here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (t.getCause() != null) {
            throw new BallerinaException(message + t.getCause().getMessage());
        } else {
            throw new BallerinaException(message + t.getMessage());
        }
    }

    @Override
    public BallerinaMessageDataSource clone() {
        OMElement clonedContent = this.value().cloneOMElement();
        BXML clonedMessage = new BXML(clonedContent);
        return clonedMessage;
    }
}
