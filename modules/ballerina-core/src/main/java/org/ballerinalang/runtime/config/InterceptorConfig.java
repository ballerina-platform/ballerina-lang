/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.runtime.config;

import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;

/**
 * Configuration BeanClass for Service Connectors's Interceptors.
 */
public class InterceptorConfig {

    @Element(description = "flag to enable server Connector interception")
    private boolean enable = false;

    @Element(description = "interceptor deployment directory", required = true)
    private String deploymentDirectory;

    @Element(description = "service interceptors list")
    private ArrayList<ServiceInterceptorConfig> serviceInterceptors = new ArrayList<>();

    public boolean isEnable() {
        return enable;
    }

    public String getDeploymentDirectory() {
        return deploymentDirectory;
    }

    public ArrayList<ServiceInterceptorConfig> getServiceInterceptors() {
        return serviceInterceptors;
    }
}
