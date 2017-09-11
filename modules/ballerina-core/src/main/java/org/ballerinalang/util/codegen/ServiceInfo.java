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

import org.ballerinalang.services.dispatchers.uri.URITemplate;
import org.ballerinalang.services.dispatchers.uri.URITemplateException;
import org.ballerinalang.services.dispatchers.uri.parser.Literal;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@code ServiceInfo} contains metadata of a Ballerina service entry in the program file.
 *
 * @since 0.87
 */
public class ServiceInfo extends StructureTypeInfo {

    private int protocolPkgPathCPIndex;
    private String protocolPkgPath;

    private Map<String, ResourceInfo> resourceInfoMap = new HashMap<>();

    private FunctionInfo initFuncInfo;
    private URITemplate uriTemplate;

    public ServiceInfo(int pkgPathCPIndex, String packageName,
                       int nameCPIndex, String serviceName,
                       int protocolPkgPathCPIndex, String protocolPkgPath) {

        super(pkgPathCPIndex, packageName, nameCPIndex, serviceName);
        this.protocolPkgPathCPIndex = protocolPkgPathCPIndex;
        this.protocolPkgPath = protocolPkgPath;
    }

    public int getProtocolPkgPathCPIndex() {
        return protocolPkgPathCPIndex;
    }

    public String getProtocolPkgPath() {
        return protocolPkgPath;
    }

    public ResourceInfo[] getResourceInfoEntries() {
        return resourceInfoMap.values().toArray(new ResourceInfo[0]);
    }

    public void addResourceInfo(String resourceName, ResourceInfo resourceInfo) {
        resourceInfoMap.put(resourceName, resourceInfo);
    }

    public ResourceInfo getResourceInfo(String resourceName) {
        return resourceInfoMap.get(resourceName);
    }

    public FunctionInfo getInitFunctionInfo() {
        return initFuncInfo;
    }

    public void setInitFunctionInfo(FunctionInfo initFuncInfo) {
        this.initFuncInfo = initFuncInfo;
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

    public AnnAttachmentInfo getAnnotationAttachmentInfo(String packageName, String annotationName) {
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) getAttributeInfo(
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo == null || packageName == null || annotationName == null) {
            return null;
        }
        for (AnnAttachmentInfo annotationInfo : attributeInfo.getAttachmentInfoEntries()) {
            if (packageName.equals(annotationInfo.getPkgPath()) && annotationName.equals(annotationInfo.getName())) {
                return annotationInfo;
            }
        }
        return null;
    }

    public URITemplate getUriTemplate() throws URITemplateException {
        if (uriTemplate == null) {
            uriTemplate = new URITemplate(new Literal("/"));
        }
        return uriTemplate;
    }
}
