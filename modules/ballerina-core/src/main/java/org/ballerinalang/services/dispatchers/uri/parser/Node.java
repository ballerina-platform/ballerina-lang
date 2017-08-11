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
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ResourceInfo resource = validateHTTPMethod(this.resource, carbonMessage);
        validateConsumes(resource, carbonMessage);
        validateProduces(resource, carbonMessage);
        return resource;
    }

    private ResourceInfo validateHTTPMethod(List<ResourceInfo> resources, CarbonMessage carbonMessage) {
        ResourceInfo resource = null;
        String httpMethod = (String) carbonMessage.getProperty(Constants.HTTP_METHOD);
        for (ResourceInfo resourceInfo : resources) {
            if (resourceInfo.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, httpMethod) != null) {
                resource =  resourceInfo;
            }
        }
        if (resource == null) {
            resource = tryMatchingToDefaultVerb(httpMethod);
        }
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
                boolean prevResourceHasMethod = validateMethodsOfSameURIResources(previousResource, newResource);
                if (!prevResourceHasMethod) {
                    validateMethodOfNewResource(newResource);
                }
            }
            this.resource.add(newResource);
        }
    }

    private boolean validateMethodsOfSameURIResources(ResourceInfo previousResource, ResourceInfo newResource) {
        boolean prevResourceHasMethod = false;
        for (String method : this.httpMethods) {
            if (previousResource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, method) != null) {
                prevResourceHasMethod = true;
                if (newResource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, method) != null) {
                    throw new BallerinaException("Seems two resources have the same addressable URI");
                }
            }
        }
        return prevResourceHasMethod;
    }

    private void validateMethodOfNewResource(ResourceInfo newResource) {
        boolean newResourceHasMethod = false;
        for (String method : this.httpMethods) {
            if (newResource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, method) != null) {
                newResourceHasMethod = true;
            }
        }
        if (!newResourceHasMethod) {
            //if both resources do not have methods but same URI, then throw following error.
            throw new BallerinaException("Seems two resources have the same addressable URI");
        }
    }

    abstract String expand(Map<String, String> variables);

    abstract int match(String uriFragment, Map<String, String> variables);

    abstract String getToken();

    abstract char getFirstCharacter();

    private Node isAlreadyExist(String token, List<Node> childList) {
        for (Node node : childList) {
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
        return null;
    }

    public ResourceInfo validateConsumes(ResourceInfo resource, CarbonMessage cMsg) {
        boolean isConsumeMatched = false;
        String contentMediaType = extractContentMediaType(cMsg.getHeader(Constants.CONTENT_TYPE_HEADER));
        AnnAttachmentInfo consumeInfo = resource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_CONSUMES);

        if (consumeInfo != null) {
            //when Content-Type header is not set, treat it as "application/octet-stream"
            contentMediaType = (contentMediaType != null ? contentMediaType : Constants.VALUE_ATTRIBUTE);
            for (AnnAttributeValue attributeValue : consumeInfo.getAttributeValue(
                    Constants.VALUE_ATTRIBUTE).getAttributeValueArray()) {
                if (contentMediaType.equals(attributeValue.getStringValue().trim())) {
                    isConsumeMatched = true;
                    break;
                }
            }
            if (!isConsumeMatched) {
                cMsg.setProperty(Constants.HTTP_STATUS_CODE, 415);
                throw new BallerinaException();
            }
        }
        return resource;
    }

    private String extractContentMediaType(String header) {
        if (header == null) {
            return null;
        } else {
            if (header.contains(";")) {
                header = header.substring(0, header.indexOf(";")).trim();
            }
        }
        return header;
    }

    public ResourceInfo validateProduces(ResourceInfo resource, CarbonMessage cMsg) {
        boolean isProduceMatched = false;
        List<String> acceptMediaTypes = extractAcceptMediaTypes(cMsg.getHeader(Constants.ACCEPT_HEADER));
        AnnAttachmentInfo produceInfo = resource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_PRODUCES);

        //If Accept header field is not present, then it is assumed that the client accepts all media types.
        if (produceInfo != null && acceptMediaTypes != null) {
            if (acceptMediaTypes.contains("*/*")) {
                isProduceMatched = true;
            } else {
                if (acceptMediaTypes.stream().anyMatch(mediaType -> mediaType.contains("/*"))) {
                    List<String> subTypeWildCardMediaTypes = acceptMediaTypes.stream()
                            .filter(mediaType -> mediaType.contains("/*"))
                            .map(mediaType -> mediaType.substring(0, mediaType.indexOf("/")))
                            .collect(Collectors.toList());
                    List<String> subAttributeValues = Arrays.stream(produceInfo
                            .getAttributeValue(Constants.VALUE_ATTRIBUTE).getAttributeValueArray())
                            .map(mediaType -> mediaType.getStringValue().trim()
                                    .substring(0, mediaType.getStringValue().indexOf("/")))
                            .distinct().collect(Collectors.toList());
                    for (String token : subAttributeValues) {
                        for (String mediaType : subTypeWildCardMediaTypes) {
                            if (mediaType.equals(token)) {
                                isProduceMatched = true;
                                break;
                            }
                        }
                    }
                }
                if (!isProduceMatched) {
                    List<String> noWildCardMediaTypes = acceptMediaTypes.stream()
                            .filter(mediaType -> !mediaType.contains("/*")).collect(Collectors.toList());
                    for (AnnAttributeValue attributeValue : produceInfo.getAttributeValue(
                            Constants.VALUE_ATTRIBUTE).getAttributeValueArray()) {
                        for (String mediaType : noWildCardMediaTypes) {
                            if (mediaType.equals(attributeValue.getStringValue())) {
                                isProduceMatched = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!isProduceMatched) {
                cMsg.setProperty(Constants.HTTP_STATUS_CODE, 406);
                throw new BallerinaException();
            }
        }
        return resource;
    }

    private List<String> extractAcceptMediaTypes(String header) {
        List<String> acceptMediaTypes = new ArrayList();
        if (header == null) {
            return null;
        } else {
            if (header.contains(",")) {
                //process headers like this: text/*;q=0.3, text/html;Level=1;q=0.7, */*
                acceptMediaTypes = Arrays.stream(header.split(","))
                        .map(mediaRange -> mediaRange.contains(";") ? mediaRange
                                .substring(0, mediaRange.indexOf(";")) : mediaRange)
                        .map(String::trim).distinct().collect(Collectors.toList());
            } else if (header.contains(";")) {
                //process headers like this: text/*;q=0.3
                acceptMediaTypes.add(header.substring(0, header.indexOf(";")).trim());
            } else {
                acceptMediaTypes.add(header.trim());
            }
        }
        return acceptMediaTypes;
    }
}
