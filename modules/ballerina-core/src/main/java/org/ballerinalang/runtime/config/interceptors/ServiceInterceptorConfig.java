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
package org.ballerinalang.runtime.config.interceptors;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration BeanClass for Service Interceptors.
 */
@Configuration(description = "Interceptor Configuration")
public class ServiceInterceptorConfig {

    @Element(description = "service connector type", required = true)
    private String serverConnector;

    @Element(description = "flag to enable server Connector interception")
    private boolean enable = false;

    @Element(description = "interceptor deployment directory", required = true)
    private String deploymentDirectory;

    @Element(description = "request interceptors")
    private ArrayList<InterceptorConfig> request = new ArrayList<>();

    @Element(description = "response interceptors")
    private ArrayList<InterceptorConfig> response = new ArrayList<>();

    public boolean isEnable() {
        return enable;
    }

    public String getServerConnector() {
        return serverConnector;
    }

    public String getDeploymentDirectory() {
        return deploymentDirectory;
    }

    public List<InterceptorConfig> getRequest() {
        return request;
    }

    public List<InterceptorConfig> getResponse() {
        return response;
    }
}
