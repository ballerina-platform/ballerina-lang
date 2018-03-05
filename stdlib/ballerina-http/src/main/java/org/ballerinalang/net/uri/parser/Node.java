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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Node represents different types of path segments in the uri-template.
 *
 * @param <DataElementType> Specific data element created by the user.
 */
public abstract class Node<DataElementType extends DataElement> {

    protected String token;
    protected DataElementType dataElement;
    protected List<Node<DataElementType>> childNodesList = new LinkedList<>();

    protected Node(DataElementType dataElement, String token) {
        this.dataElement = dataElement;
        this.token = token;
    }

    public DataElementType getDataElement() {
        return dataElement;
    }

    public Node<DataElementType> addChild(Node<DataElementType> childNode) {
        Node<DataElementType> node = childNode;
        Node<DataElementType> matchingChildNode = getMatchingChildNode(childNode, childNodesList);
        if (matchingChildNode != null) {
            node = matchingChildNode;
        } else {
            this.childNodesList.add(node);
        }

        Collections.sort(childNodesList, (o1, o2) -> getIntValue(o2) - getIntValue(o1));

        return node;
    }

    public DataElementType matchAll(String uriFragment, Map<String, String> variables, int start) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return null;
        }
        if (matchLength == uriFragment.length()) {
            return dataElement;
        }
        if (matchLength >= uriFragment.length()) {
            return null;
        }
        String subUriFragment = nextURIFragment(uriFragment, matchLength);
        String subPath = nextSubPath(subUriFragment);

        DataElementType dataElement;
        for (Node<DataElementType> childNode : childNodesList) {
            if (childNode instanceof Literal) {
                String regex = childNode.getToken();
                if (regex.equals("*")) {
                    regex = "." + regex;
                    if (!subPath.matches(regex)) {
                        continue;
                    }
                    dataElement = childNode.matchAll(subUriFragment, variables, start + matchLength);
                    if (hasDataElement(dataElement)) {
                        setUriPostFix(variables, subUriFragment);
                        return dataElement;
                    }
                    continue;
                }
                if (!subPath.contains(regex)) {
                    continue;
                }
                dataElement = childNode.matchAll(subUriFragment, variables, start + matchLength);
                if (hasDataElement(dataElement)) {
                    return dataElement;
                }
                continue;
            }
            dataElement = childNode.matchAll(subUriFragment, variables, start + matchLength);
            if (hasDataElement(dataElement)) {
                return dataElement;
            }
        }
        return null;
    }

    private boolean hasDataElement(DataElementType dataElement) {
        return dataElement != null && dataElement.hasData();
    }

    private void setUriPostFix(Map<String, String> variables, String subUriFragment) {
        variables.putIfAbsent(HttpConstants.EXTRA_PATH_INFO, "/" + subUriFragment);
    }

    abstract String expand(Map<String, String> variables);

    abstract int match(String uriFragment, Map<String, String> variables);

    abstract String getToken();

    abstract char getFirstCharacter();

    private Node<DataElementType> getMatchingChildNode(Node<DataElementType> prospectiveChild,
                                                       List<Node<DataElementType>> existingChildren) {
        boolean isExpression = prospectiveChild instanceof Expression;
        String prospectiveChildToken = prospectiveChild.getToken();

        for (Node<DataElementType> existingChild : existingChildren) {
            if (isExpression && existingChild instanceof Expression) {
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
