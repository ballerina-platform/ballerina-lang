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
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code ResourceImpl} This is the implementation for the {@code Resource} API.
 *
 * @since 0.94
 */
public class ResourceImpl extends AnnotatableNode implements Resource {
    private String name;

    //reference to the original resourceInfo object.
    private ResourceInfo resourceInfo;

    public ResourceImpl(String name, ResourceInfo resourceInfo) {
        this.name = name;
        this.resourceInfo = resourceInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getServiceName() {
        return resourceInfo.getServiceInfo().getName();
    }

    /**
     * This is a protected method to access original resourceInfo object within Ballerina.
     *
     * @return resourceInfo object.
     */
    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    @Override
    public List<Annotation> getAnnotationList(String pkgPath, String name) {
        String key = pkgPath + ":" + name;
        return annotationMap.get(key);
    }

    @Override
    public List<ParamDetail> getParamDetails() {
        LocalVariableAttributeInfo attributeInfo = (LocalVariableAttributeInfo) resourceInfo.getAttributeInfo
                (AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        List<ParamDetail> paramDetails = new ArrayList<>();
        for (LocalVariableInfo variableInfo : attributeInfo.getLocalVariableInfoEntries()) {
            paramDetails.add(new ParamDetail(variableInfo.getVariableType(), variableInfo.getVariableName()));
        }
        return paramDetails;
    }

    @Override
    public String getAnnotationEntryKey() {
        return getServiceName() + "." + name;
    }
}
