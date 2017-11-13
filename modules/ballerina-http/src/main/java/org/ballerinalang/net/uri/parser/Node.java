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
 *
 * @param <NODE_ITEM> Specific node item created by the user.
 */
public abstract class Node<NODE_ITEM extends NodeItem> {

    protected final String token;
    protected final NODE_ITEM nodeItem;
    protected final List<Node<NODE_ITEM>> childNodesList = new LinkedList<>();

    public Node(NODE_ITEM nodeItem, String token) {
        this.token = token;
        this.nodeItem = nodeItem;
    }

    abstract String expand(Map<String, String> variables);

    abstract int match(String uriFragment, Map<String, String> variables);

    abstract String getToken();

    abstract char getFirstCharacter();

    public Node<NODE_ITEM> addChild(String segment, Node<NODE_ITEM> childNode) {
        Node<NODE_ITEM> node = childNode;
        Node<NODE_ITEM> existingNode = isAlreadyExist(segment, childNodesList);
        if (existingNode != null) {
            node = existingNode;
        } else {
            this.childNodesList.add(node);
        }

        Collections.sort(childNodesList, (o1, o2) -> getIntValue(o2) - getIntValue(o1));

        return node;
    }

    public NODE_ITEM getNodeItem() {
        return this.nodeItem;
    }

    public NODE_ITEM matchAll(String uriFragment, Map<String, String> variables, int start) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return null;
        }
        if (matchLength == uriFragment.length()) {
            return nodeItem;
        }
        if (matchLength >= uriFragment.length()) {
            return null;
        }
        String subUriFragment = nextURIFragment(uriFragment, matchLength);
        String subPath = nextSubPath(subUriFragment);

        NODE_ITEM nodeItem;
        for (Node<NODE_ITEM> childNode : childNodesList) {
            if (childNode instanceof Literal) {
                String regex = childNode.getToken();
                if (regex.equals("*")) {
                    regex = "." + regex;
                    if (!subPath.matches(regex)) {
                        continue;
                    }
                    nodeItem = childNode.matchAll(subUriFragment, variables, start + matchLength);
                    if (nodeItem != null) {
                        return nodeItem;
                    }
                    continue;
                }
                if (!subPath.contains(regex)) {
                    continue;
                }
                nodeItem = childNode.matchAll(subUriFragment, variables, start + matchLength);
                if (nodeItem != null) {
                    return nodeItem;
                }
                continue;
            }
            nodeItem = childNode.matchAll(subUriFragment, variables, start + matchLength);
            if (nodeItem != null) {
                return nodeItem;
            }
        }
        return null;
    }

    private Node<NODE_ITEM> isAlreadyExist(String token, List<Node<NODE_ITEM>> childList) {
        for (Node<NODE_ITEM> node : childList) {
            if (node.getToken().equals(token)) {
                return node;
            }
        }

        return null;
    }

    private int getIntValue(Node<NODE_ITEM> node) {
        if (node instanceof Literal) {
            if (node.getToken().equals("*")) {
                return 0;
            }
            return node.getToken().length() + 5;
        } else if (node instanceof FragmentExpression) {
            return 4;
        } else if (node instanceof ReservedStringExpression) {
            return 3;
        } else if (node instanceof LabelExpression) {
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
