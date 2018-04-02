// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package ballerina.websub;

import ballerina/http;

//////////////////////////////////////////
/////// WebSub Hub Client Endpoint ///////
//////////////////////////////////////////
@Description {value:"Struct representing the WebSub Hub Client Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"httpClientEndpoint: The underlying HTTP client endpoint"}
public struct HubClientEndpoint {
    HubClientEndpointConfiguration config;
    http:ClientEndpoint httpClientEndpoint;
}

@Description {value:"Struct representing the WebSub Hub Client Endpoint configuration"}
@Field {value:"url: The URL of the target Hub"}
public struct HubClientEndpointConfiguration {
    string url;
    http:SecureSocket|null secureSocket;
    //TODO: include header, topic-resource map
}

@Description {value:"Gets called when the endpoint is being initialized during package init"}
@Param {value:"ep: The endpoint to be initialized"}
@Param {value:"config: The configuration for the endpoint"}
public function <HubClientEndpoint ep> init (HubClientEndpointConfiguration config) {
    endpoint http:ClientEndpoint httpClientEndpoint {targets:[{url:config.url, secureSocket:config.secureSocket}]};
    ep.httpClientEndpoint = httpClientEndpoint;
    ep.config = config;
}

@Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
@Param {value:"serviceType: The service attached"}
public function <HubClientEndpoint ep> register (typedesc serviceType) {
    ep.httpClientEndpoint.register(serviceType);
}

@Description {value:"Starts the registered service"}
public function <HubClientEndpoint ep> start () {
    ep.httpClientEndpoint.start();
}

@Description {value:"Returns the connector that client code uses"}
@Return {value:"The connector that client code uses"}
public function <HubClientEndpoint ep> getClient () returns (HubClientConnector) {
    HubClientConnector webSubHubClientConn = { hubUrl:ep.config.url, httpClientEndpoint:ep.httpClientEndpoint };
    return webSubHubClientConn;
}

@Description {value:"Stops the registered service"}
@Return {value:"Error occured during registration"}
public function <HubClientEndpoint ep> stop () {
    ep.httpClientEndpoint.stop();
}
