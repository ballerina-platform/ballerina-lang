/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Ballerina XML parser
 * 
 * @since 1.3.0
 */
public class BallerinaXMLParser {

    private XMLStreamReader xmlStreamReader;

    private int currentState = XMLStreamConstants.START_DOCUMENT;

    public BallerinaXMLParser(CharReader reader) {
        XMLCharReader xmlCharReader = new XMLCharReader(reader);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try {
            this.xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlCharReader);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public STNode parse() {
        try {
            while (xmlStreamReader.hasNext()) {
                int next = xmlStreamReader.next();
                switch (next) {
                    case START_ELEMENT:
                        return getSyntaxToken(SyntaxKind.LT_TOKEN);
                    case END_ELEMENT:
                        // endElement();
                        break;
                    case PROCESSING_INSTRUCTION:
                        // readPI(xmlStreamReader);
                        break;
                    case COMMENT:
                        // readComment(xmlStreamReader);
                        break;
                    case CDATA:
                    case CHARACTERS:
                        // readText(xmlStreamReader);
                        break;
                    case END_DOCUMENT:
                        // return buildDocument();
                    case DTD:
                        // handleDTD(xmlStreamReader);
                        break;
                    default:
                        assert false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private STToken getSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(new ArrayList<>());
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>());
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private static enum XMLState {
        
    }

    private static class XMLCharReader extends Reader {

        private CharReader reader;

        public XMLCharReader(CharReader reader) {
            this.reader = reader;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            this.reader.reset(off);
            if (this.reader.isEOF()) {
                return -1;
            }

            int index = 0;
            while (index < len || this.reader.isEOF()) {
                cbuf[index] = this.reader.peek();
                this.reader.advance();
                index++;
            }

            return index;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
