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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code AnnotationAttachmentInfo} represents a Ballerina annotation attachment.
 *
 * @since 0.87
 */
@Deprecated
public class AnnAttachmentInfo {

    protected int pkgPathCPIndex;
    protected String pkgPath;

    protected int nameCPIndex;
    protected String name;

    private Map<String, AnnAttributeKeyValuePair> attributeValueMap = new LinkedHashMap<>();

    public AnnAttachmentInfo(int pkgPathCPIndex, String pkgPath, int nameCPIndex, String name) {
        this.pkgPathCPIndex = pkgPathCPIndex;
        this.pkgPath = pkgPath;
        this.nameCPIndex = nameCPIndex;
        this.name = name;
    }

    public int getPkgPathCPIndex() {
        return pkgPathCPIndex;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public String getName() {
        return name;
    }

    public Map<String, AnnAttributeKeyValuePair> getAttributeValueMap() {
        return attributeValueMap;
    }

    public void addAttributeValue(int nameCPIndex, String name, AnnAttributeValue attributeValue) {
        attributeValueMap.put(name, new AnnAttributeKeyValuePair(nameCPIndex, name, attributeValue));
    }

    public AnnAttributeValue getAttributeValue(String name) {
        AnnAttributeKeyValuePair keyValuePair = attributeValueMap.get(name);
        return keyValuePair != null ? keyValuePair.getAttributeValue() : null;
    }

    public AnnAttributeKeyValuePair[] getAttributeKeyValuePairs() {
        return attributeValueMap.values().toArray(new AnnAttributeKeyValuePair[0]);
    }
}
