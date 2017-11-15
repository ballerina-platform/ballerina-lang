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

import java.util.List;

/**
 * URITemplateParser parses the provided uri-template and build the tree.
 *
 * @param <DataElementType> Specific data element type for the parser.
 * @param <DataType> Data type stored in the node item.
 * @param <CheckerType> Additional checker for node item.
 */
public class URITemplateParser<DataElementType extends DataElement<DataType, CheckerType>, DataType, CheckerType> {

    private static final char[] operators = new char[] { '+', '.', '/', ';', '?', '&', '#' };

    private Node<DataElementType> syntaxTree;
    private Node<DataElementType> currentNode;
    private final DataElementCreator<DataElementType> dataElementCreator;

    public URITemplateParser(Node<DataElementType> rootNode, DataElementCreator<DataElementType> dataElementCreator) {
        this.syntaxTree = rootNode;
        this.dataElementCreator = dataElementCreator;
    }

    public Node parse(String template, DataType data) throws URITemplateException {
        if (!"/".equals(template) && template.endsWith("/")) {
            template = template.substring(0, template.length() - 1);
        }

        if ("/".equals(template)) {
            this.syntaxTree.getDataElement().setData(data);
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
                            addNode(new Literal<>(createNodeItem(), segment.substring(startIndex, pointerIndex)));
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
                                addNode(new Literal<>(createNodeItem(), tokenVal));
                            }
                        }
                }
            }
        }
        this.currentNode.getDataElement().setData(data);

        return syntaxTree;
    }

    private <NodeType extends Node<DataElementType>> void addNode(NodeType node) {
        if (currentNode == null) {
            currentNode = syntaxTree;
        }
        node.setParentNode(currentNode);
        if (node.getToken().equals("*")) {
            currentNode = currentNode.addChild("." + node.getToken(), node);
        } else {
            currentNode = currentNode.addChild(node.getToken(), node);
        }
    }

    public void remove(String template) throws URITemplateException {
        if (!"/".equals(template) && template.endsWith("/")) {
            template = template.substring(0, template.length() - 1);
        }

        if ("/".equals(template)) {
            this.syntaxTree.getDataElement().clearData();
            return;
        }

        Node<DataElementType> currentNodePointer = syntaxTree;
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
                            String literalToken = segment.substring(startIndex, pointerIndex);
                            currentNodePointer = findBestMatchingLiteralNode(currentNodePointer.childNodesList, literalToken);
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
                        currentNodePointer =
                                findBestMatchingExpressionNode(currentNodePointer.childNodesList, token, maxIndex, pointerIndex);
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
                                currentNodePointer =
                                        findBestMatchingExpressionNode(currentNodePointer.childNodesList, tokenVal, maxIndex, pointerIndex);
                            } else {
                                currentNodePointer = findBestMatchingLiteralNode(currentNodePointer.childNodesList, tokenVal);
                            }
                        }
                }
            }
        }

        if (currentNodePointer.getChildNodesList().size() == 0) {
            Node<DataElementType> parentNodePointer = currentNodePointer.getParentNode();
            while (currentNodePointer.getChildNodesList().size() == 0) {
                parentNodePointer.getChildNodesList().remove(currentNodePointer);
                currentNodePointer = parentNodePointer.getParentNode();
            }
        } else {
            currentNodePointer.getDataElement().clearData();
        }
    }

    private Node<DataElementType> findBestMatchingLiteralNode(List<Node<DataElementType>> childNodes,
                                                              String literalToken) throws URITemplateException {
        for (Node<DataElementType> childNode : childNodes) {
            if (childNode instanceof Literal && literalToken.equals(childNode.getToken())) {
                return childNode;
            }
        }
        throw new URITemplateException("Could not find an matching element for " + literalToken);
    }

    private Node<DataElementType> findBestMatchingExpressionNode(List<Node<DataElementType>> childNodes, String expression,
                                                      int maxIndex, int pointerIndex) throws URITemplateException {
        Class expressionClass;
        if (isSimpleString(expression)) {
            if (maxIndex == pointerIndex) {
                expressionClass = SimpleStringExpression.class;
            } else {
                expressionClass = SimpleSplitStringExpression.class;
            }
        }

        if (expression.length() <= 1) {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }

        // TODO: Re-verify the usage of these nodes
        if (expression.startsWith("#")) {
            expression = expression.substring(1);
            expressionClass = FragmentExpression.class;
        } else if (expression.startsWith("+")) {
            expression = expression.substring(1);
            expressionClass = ReservedStringExpression.class;
        } else if (expression.startsWith(".")) {
            expression = expression.substring(1);
            expressionClass = LabelExpression.class;
        } else if (expression.startsWith("/")) {
            expression = expression.substring(1);
            expressionClass = PathSegmentExpression.class;
        } else {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }

        for (Node<DataElementType> childNode : childNodes) {
            if (expressionClass.isInstance(childNode) && expression.equals(childNode.getToken())) {
                return childNode;
            }
        }

        throw new URITemplateException("Invalid template expression: {" + expression + "}");
    }

    private void createExpressionNode(String expression, int maxIndex, int pointerIndex) throws URITemplateException {
        Node<DataElementType> node = null;
        if (isSimpleString(expression)) {
            if (maxIndex == pointerIndex) {
                node = new SimpleStringExpression<>(createNodeItem(), expression);
            } else {
                node = new SimpleSplitStringExpression<>(createNodeItem(), expression);
            }
        }

        if (expression.length() <= 1) {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }

        // TODO: Re-verify the usage of these nodes
        if (expression.startsWith("#")) {
            node = new FragmentExpression<>(createNodeItem(), expression.substring(1));
        } else if (expression.startsWith("+")) {
            node = new ReservedStringExpression<>(createNodeItem(), expression.substring(1));
        } else if (expression.startsWith(".")) {
            node = new LabelExpression<>(createNodeItem(), expression.substring(1));
        } else if (expression.startsWith("/")) {
            node = new PathSegmentExpression<>(createNodeItem(), expression.substring(1));
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

    private DataElementType createNodeItem() {
        return dataElementCreator.createDataElement();
    }
}
