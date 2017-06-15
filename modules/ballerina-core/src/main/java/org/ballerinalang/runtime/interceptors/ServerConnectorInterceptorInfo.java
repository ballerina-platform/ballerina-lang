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
package org.ballerinalang.runtime.interceptors;

import org.ballerinalang.util.codegen.ProgramFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ServerConnectorInterceptorInfo} represents all interception information about given server connector.
 */
public class ServerConnectorInterceptorInfo {

      Path deploymentPath;
    String serverConnector;
    List<ResourceInterceptor> requestChain = new ArrayList<>();
    List<ResourceInterceptor> responseChain = new ArrayList<>();

    // Cache values.
    Map<String, ProgramFile> programFileMap = new HashMap<>();

    public ServerConnectorInterceptorInfo(String serverConnector) {
        this.serverConnector = serverConnector;
    }

    public List<ResourceInterceptor> getRequestChain() {
        return requestChain;
    }

    public List<ResourceInterceptor> getResponseChain() {
        return responseChain;
    }
}
