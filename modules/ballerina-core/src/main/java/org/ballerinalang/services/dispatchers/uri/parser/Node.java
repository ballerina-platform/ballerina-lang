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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Node {

    protected String token;

    protected List<Node> childNodesList = new LinkedList<>();

    protected Resource resource;

    protected Node(String token) {
        this.token = token;
    }

    public Node addChild(String segment, Node childNode) {
        Node node = childNode;
        Node existingNode = isAlreadyExist(segment, childNodesList);
        if (existingNode != null) {
            node = existingNode;
        } else {
            this.childNodesList.add(node);
        }

        Collections.sort(childNodesList, (o1, o2) -> getIntValue(o2) - getIntValue(o1));

        return node;
    }

    public Resource matchAll(String uriFragment, Map<String, String> variables, int start) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return null;
        } else if (matchLength < uriFragment.length()) {
            String subUriFragment = nextURIFragment(uriFragment, matchLength);
            String subPath = nextSubPath(subUriFragment);

            Resource resource;
            for (Node childNode : childNodesList) {
                if (childNode instanceof Literal) {
                    String regex = childNode.getToken();
                    if (regex.equals("*")) {
                        regex = "." + regex;
                        if (subPath.matches(regex)) {
                            resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
                            if (resource != null) {
                                return resource;
                            }
                        }
                    } else {
                        if (subPath.contains(regex)) {
                            resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
                            if (resource != null) {
                                return resource;
                            }
                        }
                    }
                } else {
                    resource = childNode.matchAll(subUriFragment, variables, start + matchLength);
                    if (resource !=  null) {
                        return resource;
                    }
                }
            }

            return null;
        }
        else if (matchLength == uriFragment.length()) {
            return getResource();
        } else {
            return null;
        }
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    abstract String expand(Map<String,String> variables);
    abstract int match(String uriFragment, Map<String,String> variables);
    abstract String getToken();
    abstract char getFirstCharacter();

    private Node isAlreadyExist(String token, List<Node> childList) {
        for (Node node: childList) {
            if (node.getToken().equals(token)) {
                return node;
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
