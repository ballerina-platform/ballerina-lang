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

import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.uri.URITemplateException;

/**
 * URITemplateParser parses the provided uri-template and build the tree.
 */
public class URITemplateParser<Data, Checker> {

    private static final char[] operators = new char[] { '+', '.', '/', ';', '?', '&', '#' };

    private Node<DataElement<Data, Checker>> syntaxTree;
    private Node<DataElement<Data, Checker>> currentNode;
    private final DataElementCreator<? extends DataElement<Data, Checker>> elementCreator;

    public URITemplateParser(Node<DataElement<Data, Checker>> rootNode,
                             DataElementCreator<? extends DataElement<Data, Checker>> elementCreator) {
        this.syntaxTree = rootNode;
        this.elementCreator = elementCreator;
    }

    public Node<DataElement<Data, Checker>> parse(String template, Data resource) throws URITemplateException {
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
                        createExpressionNode(token, maxIndex, pointerIndex);
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
                                createExpressionNode(tokenVal, maxIndex, pointerIndex);
                            } else {
                                addNode(new Literal<>(createElement(), tokenVal));
                            }
                        }
                }
            }
        }
        this.currentNode.getDataElement().setData(resource);

        return syntaxTree;
    }

    private void addNode(Node<DataElement<Data, Checker>> node) {
        if (currentNode == null) {
            currentNode = syntaxTree;
        }
        currentNode = currentNode.addChild(node);
    }

    private void createExpressionNode(String expression, int maxIndex, int pointerIndex) throws URITemplateException {
        Node<DataElement<Data, Checker>> node = null;
        if (isSimpleString(expression)) {
            if (maxIndex == pointerIndex) {
                node = new SimpleStringExpression<>(createElement(), expression);
            } else {
                node = new SimpleSplitStringExpression<>(createElement(), expression);
            }
        }

        if (expression.length() <= 1) {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }

        // TODO: Re-verify the usage of these nodes
        if (expression.startsWith("#")) {
            node = new FragmentExpression<>(createElement(), expression.substring(1));
        } else if (expression.startsWith("+")) {
            node = new ReservedStringExpression<>(createElement(), expression.substring(1));
        } else if (expression.startsWith(".")) {
            node = new LabelExpression<>(createElement(), expression.substring(1));
        } else if (expression.startsWith("/")) {
            node = new PathSegmentExpression<>(createElement(), expression.substring(1));
        }

        if (node != null) {
            addNode(node);
        } else {
            throw new URITemplateException("Unsupported template expression: {" + expression + "}");
        }
    }

    private boolean isSimpleString(String expression) {
        for (char op : operators) {
            if (expression.indexOf(op) == 0) {
                return false;
            }
        }
        return true;
    }

    private DataElement<Data, Checker> createElement() {
        return elementCreator.createDataElement();
    }
}
