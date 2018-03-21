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
package org.ballerinalang.connector.api;

import org.ballerinalang.util.codegen.ResourceInfo;

import java.util.List;

/**
 * {@code Resource} This API provides the functionality to access Resource level details in the
 * respective server connector.
 *
 * @since 0.94
 */
public interface Resource {

    /**
     * This method returns the resource name.
     *
     * @return resource name.
     */
    String getName();

    /**
     * This method returns the corresponding service name.
     *
     * @return service name.
     */
    String getServiceName();

    /**
     * This method will return annotation for the given package path and annotation name.
     *
     * @param pkgPath of the annotation.
     * @param name  of the annotation.
     * @return matching annotations list.
     */
    List<Annotation> getAnnotationList(String pkgPath, String name);

    /**
     * This method will return parameter list of the resource.
     *
     * @return list of ParamDetail objects.
     */
    List<ParamDetail> getParamDetails();

    ResourceInfo getResourceInfo();
}
