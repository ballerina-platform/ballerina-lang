/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.values.XMLContentHolderItem;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @since 1.1.0
 */
public class StaxXMLSink extends OutputStream {
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private XMLStreamWriter xmlStreamWriter;


    public StaxXMLSink(OutputStream outputStream) {
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
        } catch (XMLStreamException e) {
            // ignore
        }
    }

    @Override
    public void write(int b) throws IOException {
        assert false;
    }

    public void write(XMLValue<?> xmlValue) {
        switch (xmlValue.getNodeType()) {
            case SEQUENCE:
                writeSeq((XMLSequence) xmlValue);
                break;
            case ELEMENT:
                writeElement((XMLItem) xmlValue);
                break;
            case TEXT:
                writeXMLText((XMLContentHolderItem) xmlValue);
                break;
            case COMMENT:
                writeXMLComment((XMLContentHolderItem) xmlValue);
                break;
            case PI:
                writeXMLPI((XMLContentHolderItem) xmlValue);
                break;
            case CDATA:
                writeXMLCDATA((XMLContentHolderItem) xmlValue);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + xmlValue.getNodeType());
        }

    }

    private void writeXMLCDATA(XMLContentHolderItem xmlValue) {
        int i = 0;

    }

    private void writeXMLPI(XMLContentHolderItem xmlValue) {
        int i = 0;
    }

    private void writeXMLComment(XMLContentHolderItem xmlValue) {
        int i = 0;

    }

    private void writeXMLText(XMLContentHolderItem xmlValue) {
        int i = 0;

    }

    private void writeElement(XMLItem xmlValue) {
        int i = 0;

    }

    private void writeSeq(XMLSequence xmlValue) {
        int i = 0;

    }
}
