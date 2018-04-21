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

import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.uri.URITemplateException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Node represents different types of path segments in the uri-template.
 *
 * @param <DataType> Type of data which should be stored in the node.
 * @param <InboundMsgType> Inbound message type for additional checks.
 */
public abstract class Node<DataType, InboundMsgType> {

    protected String token;
    protected DataElement<DataType, InboundMsgType> dataElement;
    protected List<Node<DataType, InboundMsgType>> childNodesList = new LinkedList<>();

    protected Node(DataElement<DataType, InboundMsgType> dataElement, String token) {
        this.dataElement = dataElement;
        this.token = token;
    }

    public DataElement<DataType, InboundMsgType> getDataElement() {
        return dataElement;
    }

    public Node<DataType, InboundMsgType> addChild(Node<DataType, InboundMsgType> childNode)
            throws URITemplateException {
        Node<DataType, InboundMsgType> node = childNode;
        Node<DataType, InboundMsgType> matchingChildNode = getMatchingChildNode(childNode, childNodesList);
        if (matchingChildNode != null) {
            node = matchingChildNode;
        } else {
            this.childNodesList.add(node);
        }

        Collections.sort(childNodesList, (o1, o2) -> getIntValue(o2) - getIntValue(o1));

        return node;
    }

    public boolean matchAll(String uriFragment, Map<String, String> variables,
                                                          int start, InboundMsgType inboundMsg,
                                                          DataReturnAgent<DataType> dataReturnAgent) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return false;
        }
        if (matchLength == uriFragment.length()) {
            return dataElement.getData(inboundMsg, dataReturnAgent);
        }
        if (matchLength >= uriFragment.length()) {
            return false;
        }
        String subUriFragment = nextURIFragment(uriFragment, matchLength);
        String subPath = nextSubPath(subUriFragment);

        boolean isFound;
        for (Node<DataType, InboundMsgType> childNode : childNodesList) {
            if (childNode instanceof Literal) {
                String regex = childNode.getToken();
                if (regex.equals("*")) {
                    regex = "." + regex;
                    if (!subPath.matches(regex)) {
                        continue;
                    }
                    isFound = childNode.matchAll(subUriFragment, variables, start + matchLength, inboundMsg,
                                                 dataReturnAgent);
                    if (isFound) {
                        setUriPostFix(variables, subUriFragment);
                        return true;
                    }
                    continue;
                }
                if (!subPath.contains(regex)) {
                    continue;
                }
                isFound = childNode.matchAll(subUriFragment, variables, start + matchLength, inboundMsg,
                                             dataReturnAgent);
                if (isFound) {
                    return true;
                }
                continue;
            }
            isFound = childNode.matchAll(subUriFragment, variables, start + matchLength, inboundMsg,
                                         dataReturnAgent);
            if (isFound) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDataElement(DataElement<DataType, InboundMsgType> dataElement) {
        return dataElement != null && dataElement.hasData();
    }

    private void setUriPostFix(Map<String, String> variables, String subUriFragment) {
        variables.putIfAbsent(HttpConstants.EXTRA_PATH_INFO, "/" + subUriFragment);
    }

    abstract String expand(Map<String, String> variables);

    abstract int match(String uriFragment, Map<String, String> variables);

    abstract String getToken();

    abstract char getFirstCharacter();

    @SuppressWarnings("unchecked")
    private Node<DataType, InboundMsgType> getMatchingChildNode(Node<DataType, InboundMsgType> prospectiveChild,
            List<Node<DataType, InboundMsgType>> existingChildren) throws URITemplateException {
        boolean isExpression = prospectiveChild instanceof Expression;
        String prospectiveChildToken = prospectiveChild.getToken();

        for (Node<DataType, InboundMsgType> existingChild : existingChildren) {
            if (isExpression && existingChild instanceof Expression) {
                ((Expression) existingChild).variableList.add(new Variable(prospectiveChild.token));
                existingChild.token = existingChild.token + "+" + prospectiveChild.token;
                return existingChild;
            }
            if (existingChild.getToken().equals(prospectiveChildToken)) {
                return existingChild;
            }
        }

        return null;
    }

    private int getIntValue(Node node) {
        if (node instanceof Literal) {
            if (node.getToken().equals("*")) {
                return 0;
            }
            return node.getToken().length() + 5;
        } else {
            return 1;
        }
    }

    private String nextURIFragment(String uri, int matchLength) {
        String uriFragment = uri;
        if (uriFragment.startsWith("/")) {
            uriFragment = uriFragment.substring(matchLength);
        } else if (uriFragment.contains("/")) {
            if (uriFragment.charAt(matchLength) == '/') {
                uriFragment = uriFragment.substring(matchLength + 1);
            } else {
                uriFragment = uriFragment.substring(matchLength);
            }
        } else {
            uriFragment = uriFragment.substring(matchLength);
        }
        return uriFragment;
    }

    private String nextSubPath(String uriFragment) {
        String subPath;
        if (uriFragment.contains("/")) {
            subPath = uriFragment.substring(0, uriFragment.indexOf("/"));
        } else {
            subPath = uriFragment;
        }
        return subPath;
    }
}
