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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;


/**
 * Configuration BeanClass for Server Connectors.
 */
@Configuration(description = "Server Connector Configuration")
public class ServerConnectorConfig {

    @Element(description = "service connector name. eg: ballerina.net.http", required = true)
    private String name;

    @Element(description = "protocol identifier exposed by server connector. eg: http", required = true)
    private String protocol;

    @Element(description = "Server connector interceptors' configuration")
    private InterceptorConfig interceptors = new InterceptorConfig();

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    public InterceptorConfig getInterceptors() {
        return interceptors;
    }
}
