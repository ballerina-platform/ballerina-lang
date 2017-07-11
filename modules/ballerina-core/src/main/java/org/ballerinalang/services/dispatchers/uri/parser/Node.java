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

import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Node represents different types of path segments in the uri-template.
 */
public abstract class Node {

    protected String token;
    protected List<ResourceInfo> resource;
    protected boolean isFirstTraverse = true;
    protected List<Node> childNodesList = new LinkedList<>();
    protected String[] httpMethods = {"GET", "PUT", "POST", "DELETE", "OPTIONS", "HEAD"};

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

    public ResourceInfo matchAll(String uriFragment, Map<String, String> variables, CarbonMessage carbonMessage,
            int start) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return null;
        } else if (matchLength < uriFragment.length()) {
            String subUriFragment = nextURIFragment(uriFragment, matchLength);
            String subPath = nextSubPath(subUriFragment);

            ResourceInfo resource;
            for (Node childNode : childNodesList) {
                if (childNode instanceof Literal) {
                    String regex = childNode.getToken();
                    if (regex.equals("*")) {
                        regex = "." + regex;
                        if (subPath.matches(regex)) {
                            resource = childNode.matchAll(subUriFragment, variables,
                                    carbonMessage, start + matchLength);
                            if (resource != null) {
                                return resource;
                            }
                        }
                    } else {
                        if (subPath.contains(regex)) {
                            resource = childNode.matchAll(subUriFragment, variables, carbonMessage,
                                    start + matchLength);
                            if (resource != null) {
                                return resource;
                            }
                        }
                    }
                } else {
                    resource = childNode.matchAll(subUriFragment, variables, carbonMessage,
                            start + matchLength);
                    if (resource !=  null) {
                        return resource;
                    }
                }
            }

            return null;
        } else if (matchLength == uriFragment.length()) {
            return getResource(carbonMessage);
        } else {
            return null;
        }
    }

    public ResourceInfo getResource(CarbonMessage carbonMessage) {
        if (this.resource == null) {
            return null;
        }
        String httpMethod = (String) carbonMessage.getProperty(Constants.HTTP_METHOD);
        for (ResourceInfo resourceInfo : this.resource) {
            if (resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, httpMethod) != null) {
                return resourceInfo;
            }
        }
        ResourceInfo resource = tryMatchingToDefaultVerb(httpMethod);
        if (resource == null) {
            carbonMessage.setProperty(Constants.HTTP_STATUS_CODE, 405);
            throw new BallerinaException();
        }
        return resource;
    }

    public void setResource(ResourceInfo newResource) {
        if (isFirstTraverse) {
            this.resource = new ArrayList<>();
            this.resource.add(newResource);
            isFirstTraverse = false;
        } else {
            for (ResourceInfo previousResource: this.resource) {
                for (String methods : this.httpMethods) {
                    if (previousResource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, methods) != null) {
                        if (newResource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, methods) != null) {
                            throw new BallerinaException("Seems two resources have the same addressable URI");
                        }
                    }
                }
            }
            this.resource.add(newResource);
        }
    }

    abstract String expand(Map<String, String> variables);
    abstract int match(String uriFragment, Map<String, String> variables);
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

    private ResourceInfo tryMatchingToDefaultVerb(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            for (ResourceInfo resourceInfo : this.resource) {
                boolean isMethodAnnotationFound = false;
                for (String httpMethod : this.httpMethods) {
                    if (resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, httpMethod) != null) {
                        isMethodAnnotationFound = true;
                        break;
                    }
                }
                if (!isMethodAnnotationFound) {
                    return resourceInfo;
                }
            }
        }
        return null;
    }
}
