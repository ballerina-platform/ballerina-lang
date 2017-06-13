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

package org.ballerinalang.services.dispatchers.uri.parser;


import org.ballerinalang.model.Resource;
import org.ballerinalang.services.dispatchers.uri.URITemplateException;
import org.ballerinalang.util.codegen.ResourceInfo;

public class URITemplateParser {

    private static final char[] operators = new char[] { '+', '.', '/', ';', '?', '&', '#' };

    private Node syntaxTree;
    private Node currentNode;

    public URITemplateParser(Node rootNode) {
        this.syntaxTree = rootNode;
    }

    public Node parse(String template, ResourceInfo resource) throws URITemplateException {
        if (!"/".equals(template) && template.endsWith("/")) {
            template = template.substring(0, template.length() - 1);
        }

        if (!"/".equals(template)) {
            for (String segment : template.split("/")) {

                boolean expression = false;
                int startIndex = 0;
                int maxIndex = segment.length() - 1;

                for (int pointerIndex = 0; pointerIndex < segment.length(); pointerIndex++) {
                    char ch = segment.charAt(pointerIndex);

                    switch (ch) {
                    case '{':
                        if (!expression) {
                            if (pointerIndex + 1 >= maxIndex) {
                                throw new URITemplateException("Illegal open brace character");
                            }

                            expression = true;
                            if (pointerIndex > startIndex) {
                                addNode(new Literal(segment.substring(startIndex, pointerIndex)));
                                startIndex = pointerIndex + 1;
                                // TODO: Check whether we really need this.
                        /*} else if (segment.charAt(pointerIndex - 1) != '}') {
                            throw new URITemplateException("Illegal empty literal");*/
                            } else {
                                startIndex++;
                            }
                        } else {
                            throw new URITemplateException("Already in expression");
                        }
                        break;

                    case '}':
                        if (expression) {
                            expression = false;
                            if (pointerIndex > startIndex) {
                                createExpressionNode(segment.substring(startIndex, pointerIndex));
                                startIndex = pointerIndex + 1;
                            } else {
                                throw new URITemplateException("Illegal empty expression");
                            }
                        } else {
                            throw new URITemplateException("Illegal closing brace detected");
                        }
                        break;

                    default:
                        if (pointerIndex == maxIndex) {
                            String token = segment.substring(startIndex);
                            if (expression) {
                                createExpressionNode(token);
                            } else {
                                addNode(new Literal(token));
                            }
                        }
                    }
                }
            }
            this.currentNode.setResource(resource);
        } else {
            this.syntaxTree.setResource(resource);
        }
        return syntaxTree;
    }

    private void addNode(Node node) {
        if (currentNode == null) {
            currentNode = syntaxTree;
        }
        if (node.getToken().equals("*")) {
            currentNode = currentNode.addChild("." + node.getToken(), node);
        } else {
            currentNode = currentNode.addChild(node.getToken(), node);
        }
    }

    private void createExpressionNode(String expression) throws URITemplateException {
        Node node = null;
        if (isSimpleString(expression)) {
            node = new SimpleStringExpression(expression);
        }

        if (expression.length() <= 1) {
            throw new URITemplateException("Invalid template expression: {" + expression + "}");
        }

        if (expression.startsWith("#")) {
            node = new FragmentExpression(expression.substring(1));
        } else if (expression.startsWith("+")) {
            node = new ReservedStringExpression(expression.substring(1));
        } else if (expression.startsWith(".")) {
            node = new LabelExpression(expression.substring(1));
        } else if (expression.startsWith("/")) {
            node = new PathSegmentExpression(expression.substring(1));
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
}
