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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Node represents different types of path segments in the uri-template.
 */
public class Node<T extends NodeItem> {

    private final T item;
    private final List<Node<T>> childNodesList = new LinkedList<>();
    private NodeExpression nodeExpression;

    protected Node(T nodeItem, NodeExpression nodeExpression) {
        this.item = nodeItem;
        this.nodeExpression = nodeExpression;
    }

    public NodeExpression getNodeExpression() {
        return this.nodeExpression;
    }

    public Node<T> addChild(String segment, Node<T> childNode) {
        Node<T> node = childNode;
        Node<T> existingNode = isAlreadyExist(segment, childNodesList);
        if (existingNode != null) {
            node = existingNode;
        } else {
            this.childNodesList.add(node);
        }

        Collections.sort(childNodesList, (o1, o2) -> getIntValue(o2) - getIntValue(o1));

        return node;
    }

    public T getNodeItem() {
        return this.item;
    }

    public T matchAll(String uriFragment, Map<String, String> variables, int start) {
        int matchLength = nodeExpression.match(childNodesList, uriFragment, variables);
        if (matchLength < 0) {
            return null;
        }
        if (matchLength == uriFragment.length()) {
            return item;
        }
        if (matchLength >= uriFragment.length()) {
            return null;
        }
        String subUriFragment = nextURIFragment(uriFragment, matchLength);
        String subPath = nextSubPath(subUriFragment);

        T resource;
        for (Node<T> childNode : childNodesList) {
            if (childNode.getNodeExpression() instanceof Literal) {
                String regex = childNode.getNodeExpression().getToken();
                if (regex.equals("*")) {
                    regex = "." + regex;
                    if (!subPath.matches(regex)) {
                        continue;
                    }
                    resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
                    if (resource != null) {
                        return resource;
                    }
                    continue;
                }
                if (!subPath.contains(regex)) {
                    continue;
                }
                resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
                if (resource != null) {
                    return resource;
                }
                continue;
            }
            resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    private Node<T> isAlreadyExist(String token, List<Node<T>> childList) {
        for (Node<T> node : childList) {
            if (node.getNodeExpression().getToken().equals(token)) {
                return node;
            }
        }

        return null;
    }

    private int getIntValue(Node<T> node) {
        if (node.getNodeExpression() instanceof Literal) {
            if (node.getNodeExpression().getToken().equals("*")) {
                return 0;
            }
            return node.getNodeExpression().getToken().length() + 5;
        } else if (node.getNodeExpression() instanceof FragmentExpression) {
            return 4;
        } else if (node.getNodeExpression() instanceof ReservedStringExpression) {
            return 3;
        } else if (node.getNodeExpression() instanceof LabelExpression) {
            return 2;
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
