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

import org.ballerinalang.util.codegen.ServiceInfo;

import java.util.List;

/**
 * {@code Service} This API provides the functionality to access Service level details in the
 * respective server connector.
 *
 * @since 0.94
 */
public interface Service {

    /**
     * This method returns the service name.
     *
     * @return name of the service.
     */
    String getName();

    /**
     * This method returns the package of the service.
     *
     * @return package of the service.
     */
    String getPackage();

    /**
     * This method returns the package qualified endpoint name that bound to the service.
     *
     * @return the package qualified endpoint name that bound to the service.
     */
    String getEndpointName();

    /**
     * This method will return the list of annotations for the given package path and annotation name.
     *
     * @param pkgPath of the annotation.
     * @param name  of the annotation.
     * @return matching annotations list.
     */
    List<Annotation> getAnnotationList(String pkgPath, String name);

    /**
     * This method will return Resources associated with the service as a array.
     *
     * @return resources array.
     */
    Resource[] getResources();

    ServiceInfo getServiceInfo();

    /**
     * This method returns the version of the package.
     *
     * @return version of the package.
     */
    String getPackageVersion();
}
