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

import java.util.Objects;

/**
 * {@code ResourceInfo} contains metadata of a Ballerina resource entry in the program file.
 *
 * @since 0.87
 */
public class ResourceInfo extends CallableUnitInfo {

    protected String[] paramNames;
    private ServiceInfo serviceInfo;

    public ResourceInfo(String pkgPath, int pkgCPIndex, String actionName, int resNameCPIndex) {
        this.pkgPath = pkgPath;
        this.pkgCPIndex = pkgCPIndex;
        this.name = actionName;
        this.nameCPIndex = resNameCPIndex;

        codeAttributeInfo = new CodeAttributeInfo();
        attributeInfoMap.put(AttributeInfo.CODE_ATTRIBUTE, codeAttributeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgCPIndex, nameCPIndex);
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
}
