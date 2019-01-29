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

import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.87
 */
public class CustomTypeInfo implements AttributeInfoPool {
    protected int pkgPathCPIndex;
    protected String packagePath;

    protected int nameCPIndex;
    protected String name;

    public int flags;

    private PackageInfo packageInfo;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    public CustomTypeInfo(int pkgPathCPIndex, String packagePath,
                          int nameCPIndex, String name, int flags) {
        this.pkgPathCPIndex = pkgPathCPIndex;
        this.packagePath = packagePath;
        this.nameCPIndex = nameCPIndex;
        this.name = name;
        this.flags = flags;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
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
        // Update Cache values.
//        name = ((UTF8CPEntry) packageInfo.getCPEntry(nameCPIndex)).getValue();
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
