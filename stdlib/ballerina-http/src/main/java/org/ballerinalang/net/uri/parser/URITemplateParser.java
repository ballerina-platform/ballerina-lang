/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.net.uri.parser;

import org.ballerinalang.net.uri.URITemplateException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * URITemplateParser parses the provided uri-template and build the tree.
 *
 * @param <DataType> Data type stored in the data element.
 * @param <InboundMgsType> Inbound message type for additional checks.
 */
public class URITemplateParser<DataType, InboundMgsType> {

    private static final char[] operators = new char[] { '+', '.', '/', ';', '?', '&', '#' };

    private Node<DataType, InboundMgsType> syntaxTree;
    private Node<DataType, InboundMgsType> currentNode;
    private final DataElementFactory<? extends DataElement<DataType, InboundMgsType>> elementCreator;

    public URITemplateParser(Node<DataType, InboundMgsType> rootNode,
                             DataElementFactory<? extends DataElement<DataType, InboundMgsType>> elementCreator) {
        this.syntaxTree = rootNode;
        this.elementCreator = elementCreator;
    }

    public Node<DataType, InboundMgsType> parse(String template, DataType resource)
            throws URITemplateException, UnsupportedEncodingException {
        if (!"/".equals(template) && template.endsWith("/")) {
            template = template.substring(0, template.length() - 1);
        }

        if ("/".equals(template)) {
            this.syntaxTree.getDataElement().setData(resource);
            return syntaxTree;
        }
        String[] segments = template.split("/");
        for (int currentElement = 0; currentElement < segments.length; currentElement++) {
            String segment = segments[currentElement];
            boolean expression = false;
            int startIndex = 0;
            int maxIndex = segment.length() - 1;

            for (int pointerIndex = 0; pointerIndex < segment.length(); pointerIndex++) {
                char ch = segment.charAt(pointerIndex);
                switch (ch) {
                    case '{':
                        if (expression) {
                            throw new URITemplateException("Already in expression");
                        }
                        if (pointerIndex + 1 >= maxIndex) {
                            throw new URITemplateException("Illegal open brace character");
                        }
                        expression = true;
                        if (pointerIndex > startIndex) {
                            addNode(new Literal<>(createElement(), segment.substring(startIndex, pointerIndex)));
                            startIndex = pointerIndex + 1;
                            // TODO: Check whether we really need this.
                        /*} else if (segment.charAt(pointerIndex - 1) != '}') {
                            throw new URITemplateException("Illegal empty literal");*/
                        } else {
                            startIndex++;
                        }
                        break;
                    case '}':
                        if (!expression) {
                            throw new URITemplateException("Illegal closing brace detected");
                        }
                        if (pointerIndex <= startIndex) {
                            throw new URITemplateException("Illegal empty expression");
                        }
                        expression = false;
                        String token = segment.substring(startIndex, pointerIndex);
                        createExpressionNode(token);
                        startIndex = pointerIndex + 1;
                        break;
                    case '*':
                        if (pointerIndex == 0 && currentElement != segments.length - 1) {
                            throw new URITemplateException("/* is only allowed at the end of the Path");
                        }
                        // fallthru
                    default:
                        //TODO change below as well
                        if (pointerIndex == maxIndex) {
                            String tokenVal = segment.substring(startIndex);
                            if (expression) {
                                createExpressionNode(tokenVal);
                            } else {
                                tokenVal = URLDecoder.decode(tokenVal, StandardCharsets.UTF_8.name());
                                addNode(new Literal<>(createElement(), tokenVal));
                            }
                        }
                }
            }
        }
        this.currentNode.getDataElement().setData(resource);

        return syntaxTree;
    }

    private void addNode(Node<DataType, InboundMgsType> node) throws URITemplateException {
        if (currentNode == null) {
            currentNode = syntaxTree;
        }
        currentNode = currentNode.addChild(node);
    }

    private void createExpressionNode(String expression) throws URITemplateException {
        Node<DataType, InboundMgsType> node;
        node = new SimpleStringExpression<>(createElement(), expression);
        if (expression.length() <= 1) {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }
        addNode(node);
    }

    private DataElement<DataType, InboundMgsType> createElement() {
        return elementCreator.createDataElement();
    }
}
