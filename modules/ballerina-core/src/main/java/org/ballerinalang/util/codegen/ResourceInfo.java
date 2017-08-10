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
package org.ballerinalang.util.codegen;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code ResourceInfo} contains metadata of a Ballerina resource entry in the program file.
 *
 * @since 0.87
 */
public class ResourceInfo extends CallableUnitInfo {

    private int[] paramNameCPIndexes;
    private String[] paramNames;

    private ServiceInfo serviceInfo;

    // below are cache values.
    private String[] httpMethods;
    private String[] consumesList;
    private String[] producesList;
    private Map<String, List<String>> corsHeaders;

    public ResourceInfo(int pkgCPIndex, String pkgPath, int resNameCPIndex, String actionName) {
        this.pkgPath = pkgPath;
        this.pkgCPIndex = pkgCPIndex;
        this.name = actionName;
        this.nameCPIndex = resNameCPIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgCPIndex, nameCPIndex);
    }

    public int[] getParamNameCPIndexes() {
        return paramNameCPIndexes;
    }

    public void setParamNameCPIndexes(int[] paramNameCPIndexes) {
        this.paramNameCPIndexes = paramNameCPIndexes;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResourceInfo
                && pkgCPIndex == (((ResourceInfo) obj).pkgCPIndex)
                && nameCPIndex == (((ResourceInfo) obj).nameCPIndex);
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public boolean isMatchingMethodExist(String method) {
        if (httpMethods == null) {
            return false;
        }
        for (String value : httpMethods) {
            if (value.equals(method)) {
                return true;
            }
        }
        return false;
    }

    public String[] getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(String[] httpMethods) {
        this.httpMethods = httpMethods;
    }

    public String[] getConsumesList() {
        return consumesList;
    }

    public void setConsumesList(String[] consumesList) {
        this.consumesList = consumesList;
    }

    public String[] getProducesList() {
        return producesList;
    }

    public void setProducesList(String[] producesList) {
        this.producesList = producesList;
    }

    public void setCorsHeaders(Map<String, List<String>> corsHeaders) {
        this.corsHeaders = corsHeaders;
    }

    public Map<String, List<String>> getCorsHeaders() {
        return corsHeaders;
    }
}
