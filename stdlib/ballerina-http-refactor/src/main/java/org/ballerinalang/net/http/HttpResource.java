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

import org.ballerinalang.connector.api.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code HttpResource} This is the http wrapper for the {@code Resource} implementation.
 *
 * @since 0.94
 */
public class HttpResource {
    private Resource balResource;
    private List<String> methods;
    private String path;
    private String entityBodyAttribute;
    private List<String> consumes;
    private List<String> produces;
    private List<String> producesSubTypes;
    private CorsHeaders corsHeaders;
    private SignatureParams signatureParams;

    public HttpResource(Resource resource) {
        this.balResource = resource;
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

    public void prepareAndValidateSignatureParams() {
        signatureParams = new SignatureParams(this, balResource.getParamDetails());
        signatureParams.validate();
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

    public void setPath(String path) {
        this.path = path;
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

    public String getEntityBodyAttributeValue() {
        return entityBodyAttribute;
    }

    public void setEntityBodyAttributeValue(String entityBodyAttribute) {
        this.entityBodyAttribute = entityBodyAttribute;
    }
}
