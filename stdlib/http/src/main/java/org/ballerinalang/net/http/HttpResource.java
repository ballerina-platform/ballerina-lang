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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.transactions.TransactionConstants;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.net.http.HttpConstants.ANN_FIELD_PATH_PARAM_ORDER;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_INTERRUPTIBLE;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_PARAM_ORDER_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_RESOURCE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.PACKAGE_BALLERINA_BUILTIN;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpUtil.checkConfigAnnotationAvailability;

/**
 * {@code HttpResource} This is the http wrapper for the {@code Resource} implementation.
 *
 * @since 0.94
 */
public class HttpResource {

    private static final Logger log = LoggerFactory.getLogger(HttpResource.class);

    private static final BString METHODS_FIELD = BStringUtils.fromString("methods");
    private static final BString PATH_FIELD = BStringUtils.fromString("path");
    private static final BString BODY_FIELD = BStringUtils.fromString("body");
    private static final BString CONSUMES_FIELD = BStringUtils.fromString("consumes");
    private static final BString PRODUCES_FIELD = BStringUtils.fromString("produces");
    private static final BString CORS_FIELD = BStringUtils.fromString("cors");
    private static final BString TRANSACTION_INFECTABLE_FIELD = BStringUtils.fromString("transactionInfectable");

    private AttachedFunctionType balResource;
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
    private boolean interruptible;

    private boolean transactionAnnotated = false;

    protected HttpResource(AttachedFunctionType resource, HttpService parentService) {
        this.balResource = resource;
        this.parentService = parentService;
        this.producesSubTypes = new ArrayList<>();
    }

    public boolean isTransactionAnnotated() {
        return transactionAnnotated;
    }

    public String getName() {
        return balResource.getName();
    }

    public String getServiceName() {
        return balResource.parentObjectType.getName();
    }

    public SignatureParams getSignatureParams() {
        return signatureParams;
    }

    public HttpService getParentService() {
        return parentService;
    }

    public AttachedFunctionType getBalResource() {
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
                                                .map(mediaType -> mediaType.trim().substring(0, mediaType.indexOf('/')))
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

    public boolean isInterruptible() {
        return interruptible;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    public String getEntityBodyAttributeValue() {
        return entityBodyAttribute;
    }

    public void setEntityBodyAttributeValue(String entityBodyAttribute) {
        this.entityBodyAttribute = entityBodyAttribute;
    }

    public static HttpResource buildHttpResource(AttachedFunctionType resource, HttpService httpService) {
        HttpResource httpResource = new HttpResource(resource, httpService);
        BMap resourceConfigAnnotation = getResourceConfigAnnotation(resource);
        httpResource.setInterruptible(httpService.isInterruptible() || hasInterruptibleAnnotation(resource));

        setupTransactionAnnotations(resource, httpResource);
        if (checkConfigAnnotationAvailability(resourceConfigAnnotation)) {
            httpResource.setPath(resourceConfigAnnotation.getStringValue(PATH_FIELD).getValue().replaceAll(
                    HttpConstants.REGEX, HttpConstants.SINGLE_SLASH));
            httpResource.setMethods(
                    getAsStringList(resourceConfigAnnotation.getBArray(METHODS_FIELD).getStringArray()));
            httpResource.setConsumes(
                    getAsStringList(resourceConfigAnnotation.getBArray(CONSUMES_FIELD).getStringArray()));
            httpResource.setProduces(
                    getAsStringList(resourceConfigAnnotation.getBArray(PRODUCES_FIELD).getStringArray()));
            httpResource.setEntityBodyAttributeValue(resourceConfigAnnotation.getStringValue(BODY_FIELD).getValue());
            httpResource.setCorsHeaders(CorsHeaders.buildCorsHeaders(resourceConfigAnnotation.getMapValue(CORS_FIELD)));
            httpResource
                    .setTransactionInfectable(resourceConfigAnnotation.getBooleanValue(TRANSACTION_INFECTABLE_FIELD));

            processResourceCors(httpResource, httpService);
            httpResource.prepareAndValidateSignatureParams();
            return httpResource;
        }

        if (log.isDebugEnabled()) {
            log.debug("resourceConfig not specified in the Resource instance, using default sub path");
        }
        httpResource.setPath(resource.getName());
        httpResource.prepareAndValidateSignatureParams();
        return httpResource;
    }

    private static void setupTransactionAnnotations(AttachedFunctionType resource, HttpResource httpResource) {
        BMap transactionConfigAnnotation = HttpUtil.getTransactionConfigAnnotation(resource,
                        TransactionConstants.TRANSACTION_PACKAGE_PATH);
        if (transactionConfigAnnotation != null) {
            httpResource.transactionAnnotated = true;
        }
    }

    /**
     * Get the `BMap` resource configuration of the given resource.
     *
     * @param resource The resource
     * @return the resource configuration of the given resource
     */
    public static BMap getResourceConfigAnnotation(AttachedFunctionType resource) {
        return (BMap) resource.getAnnotation(PROTOCOL_PACKAGE_HTTP, ANN_NAME_RESOURCE_CONFIG);
    }

    protected static BMap getPathParamOrderMap(AttachedFunctionType resource) {
        Object annotation = resource.getAnnotation(PROTOCOL_PACKAGE_HTTP, ANN_NAME_PARAM_ORDER_CONFIG);
        return annotation == null ? new BMap<BString, Object>() :
                (BMap<BString, Object>) ((BMap<BString, Object>) annotation).get(ANN_FIELD_PATH_PARAM_ORDER);
    }

    private static boolean hasInterruptibleAnnotation(AttachedFunctionType resource) {
        return resource.getAnnotation(PACKAGE_BALLERINA_BUILTIN, ANN_NAME_INTERRUPTIBLE) != null;
    }

    private static List<String> getAsStringList(Object[] values) {
        if (values == null) {
            return null;
        }
        List<String> valuesList = new ArrayList<>();
        for (Object val : values) {
            valuesList.add(val.toString().trim());
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
        signatureParams = new SignatureParams(this);
        signatureParams.validate();
    }

    public List<Type> getParamTypes() {
        List<Type> paramTypes = new ArrayList<>();
        paramTypes.addAll(Arrays.asList(this.balResource.getParameterType()));
        return paramTypes;
    }
}
