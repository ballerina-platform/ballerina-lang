/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;

/**
 * {@code HttpResource} This is the http wrapper for the {@code Resource} implementation.
 *
 * @since 0.94
 */
public class HttpResource {

    private static final Logger log = LoggerFactory.getLogger(HttpResource.class);

    private static final String METHODS_FIELD = "methods";
    private static final String PATH_FIELD = "path";
    private static final String BODY_FIELD = "body";
    private static final String CONSUMES_FIELD = "consumes";
    private static final String PRODUCES_FIELD = "produces";
    private static final String CORS_FIELD = "cors";
    private static final String TRANSACTION_INFECTABLE_FIELD = "transactionInfectable";

    private Resource balResource;
    private List<String> methods;
    private String path;
    private String entityBodyAttribute;
    private List<String> consumes;
    private List<String> produces;
    private List<String> producesSubTypes;
    private CorsHeaders corsHeaders;
    private SignatureParams signatureParams;
    private HttpService parentService;
    private boolean transactionInfectable = true; //default behavior

    protected HttpResource(Resource resource, HttpService parentService) {
        this.balResource = resource;
        this.parentService = parentService;
        this.producesSubTypes = new ArrayList<>();
    }

    public String getName() {
        return balResource.getName();
    }

    public String getServiceName() {
        return balResource.getServiceName();
    }

    public SignatureParams getSignatureParams() {
        return signatureParams;
    }

    public HttpService getParentService() {
        return parentService;
    }

    public Resource getBalResource() {
        return balResource;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            log.debug("Path not specified in the Resource instance, using default sub path");
            path = balResource.getName();
        } else {
            path = resourcePath;
        }
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;

        if (produces != null) {
            List<String> subAttributeValues = produces.stream()
                                                .map(mediaType -> mediaType.trim().substring(0, mediaType.indexOf("/")))
                                                .distinct()
                                                .collect(Collectors.toList());
            setProducesSubTypes(subAttributeValues);
        }
    }

    public List<String> getProducesSubTypes() {
        return producesSubTypes;
    }

    public void setProducesSubTypes(List<String> producesSubTypes) {
        this.producesSubTypes = producesSubTypes;
    }

    public CorsHeaders getCorsHeaders() {
        return corsHeaders;
    }

    public void setCorsHeaders(CorsHeaders corsHeaders) {
        this.corsHeaders = corsHeaders;
    }

    public boolean isTransactionInfectable() {
        return transactionInfectable;
    }

    public void setTransactionInfectable(boolean transactionInfectable) {
        this.transactionInfectable = transactionInfectable;
    }

    public String getEntityBodyAttributeValue() {
        return entityBodyAttribute;
    }

    public void setEntityBodyAttributeValue(String entityBodyAttribute) {
        this.entityBodyAttribute = entityBodyAttribute;
    }

    public static HttpResource buildHttpResource(Resource resource, HttpService httpService) {
        HttpResource httpResource = new HttpResource(resource, httpService);
        Annotation resourceConfigAnnotation = getResourceConfigAnnotation(resource);

        if (resourceConfigAnnotation == null) {
            if (log.isDebugEnabled()) {
                log.debug("resourceConfig not specified in the Resource instance, using default sub path");
            }
            httpResource.setPath(resource.getName());
            httpResource.prepareAndValidateSignatureParams();
            return httpResource;
        }

        Struct resourceConfig = resourceConfigAnnotation.getValue();

        httpResource.setPath(resourceConfig.getStringField(PATH_FIELD));
        httpResource.setMethods(getAsStringList(resourceConfig.getArrayField(METHODS_FIELD)));
        httpResource.setConsumes(getAsStringList(resourceConfig.getArrayField(CONSUMES_FIELD)));
        httpResource.setProduces(getAsStringList(resourceConfig.getArrayField(PRODUCES_FIELD)));
        httpResource.setEntityBodyAttributeValue(resourceConfig.getStringField(BODY_FIELD));
        httpResource.setCorsHeaders(CorsHeaders.buildCorsHeaders(resourceConfig.getStructField(CORS_FIELD)));
        httpResource.setTransactionInfectable(resourceConfig.getBooleanField(TRANSACTION_INFECTABLE_FIELD));

        processResourceCors(httpResource, httpService);
        httpResource.prepareAndValidateSignatureParams();
        return httpResource;
    }

    protected static Annotation getResourceConfigAnnotation(Resource resource) {
        List<Annotation> annotationList = resource.getAnnotationList(HTTP_PACKAGE_PATH, ANN_NAME_RESOURCE_CONFIG);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple resource configuration annotations found in resource: " +
                            resource.getServiceName() + "." + resource.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    private static List<String> getAsStringList(Value[] values) {
        if (values == null) {
            return null;
        }
        List<String> valuesList = new ArrayList<>();
        for (Value val : values) {
            valuesList.add(val.getStringValue().trim());
        }
        return !valuesList.isEmpty() ? valuesList : null;
    }

    private static void processResourceCors(HttpResource resource, HttpService service) {
        CorsHeaders corsHeaders = resource.getCorsHeaders();
        if (!corsHeaders.isAvailable()) {
            //resource doesn't have CORS headers, hence use service CORS
            resource.setCorsHeaders(service.getCorsHeaders());
            return;
        }

        if (corsHeaders.getAllowOrigins() == null) {
            corsHeaders.setAllowOrigins(Stream.of("*").collect(Collectors.toList()));
        }

        if (corsHeaders.getAllowMethods() != null) {
            return;
        }

        if (resource.getMethods() != null) {
            corsHeaders.setAllowMethods(resource.getMethods());
            return;
        }
        corsHeaders.setAllowMethods(DispatcherUtil.addAllMethods());
    }

    private void prepareAndValidateSignatureParams() {
        signatureParams = new SignatureParams(this, balResource.getParamDetails());
        signatureParams.validate();
    }

}
