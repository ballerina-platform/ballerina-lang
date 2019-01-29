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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code ServiceInfo} contains metadata of a Ballerina service entry in the program file.
 *
 * @since 0.87
 */
public class ServiceInfo implements AttributeInfoPool {

    protected int pkgPathCPIndex;
    protected String packagePath;

    protected int nameCPIndex;
    protected String name;

    public int flags;

    public TypeRefCPEntry serviceType;

    private Map<String, FunctionInfo> resourceInfoMap;
    private List<String> resourceNameList = new ArrayList<>();

    private PackageInfo packageInfo;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    // cached values.
    public BMap serviceValue;

    public ServiceInfo(int pkgPathCPIndex, String packageName, int nameCPIndex, String serviceName, int flags,
            TypeRefCPEntry serviceType) {
        this.pkgPathCPIndex = pkgPathCPIndex;
        this.packagePath = packageName;
        this.nameCPIndex = nameCPIndex;
        this.name = serviceName;
        this.flags = flags;

        this.serviceType = serviceType;
    }

    public FunctionInfo[] getResourceInfoEntries() {
        if (resourceInfoMap == null) {
            resourceInfoMap = new HashMap<>();
            for (String name : resourceNameList) {
                resourceInfoMap.put(name, packageInfo.getFunctionInfo(name));
            }
        }
        return resourceInfoMap.values().toArray(new FunctionInfo[0]);
    }

    public void addResourceInfo(String resourceName) {
        resourceNameList.add(resourceName);
    }

    public FunctionInfo getResourceInfo(String resourceName) {
        return resourceInfoMap.get(resourceName);
    }

    public String getName() {
        return name;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    protected void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public BType getType() {
        return serviceType.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgPathCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ServiceInfo
                && pkgPathCPIndex == (((ServiceInfo) obj).pkgPathCPIndex)
                && nameCPIndex == (((ServiceInfo) obj).nameCPIndex);
    }

    @Override
    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    @Override
    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }
}
