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

@Description {value:"Object representing the WebSub Hub Client Endpoint"}
@Field {value:"config: The configuration for the endpoint"}
@Field {value:"httpClientEndpoint: The underlying HTTP client endpoint"}
public type Client object {

    public {
        HubClientEndpointConfiguration config;
    }

    private {
        http:Client httpClientEndpoint;
    }

    @Description {value:"Gets called when the endpoint is being initialized during package init"}
    @Param {value:"ep: The endpoint to be initialized"}
    @Param {value:"config: The configuration for the endpoint"}
    public function init (HubClientEndpointConfiguration config) {
        endpoint http:Client httpClientEndpoint {targets:[{url:config.url, secureSocket:config.secureSocket}]};
        self.httpClientEndpoint = httpClientEndpoint;
        self.config = config;
    }

    @Description {value:"Gets called whenever a service attaches itself to this endpoint and during package init"}
    @Param {value:"serviceType: The service attached"}
    public function register (typedesc serviceType) {
        httpClientEndpoint.register(serviceType);
    }

    @Description {value:"Starts the registered service"}
    public function start () {
        httpClientEndpoint.start();
    }

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getClient () returns (HubClientConnector) {
        //TODO: create a single object - move to init
        HubClientConnector webSubHubClientConn = new HubClientConnector(config.url, httpClientEndpoint);
        return webSubHubClientConn;
    }

    @Description {value:"Stops the registered service"}
    @Return {value:"Error occured during registration"}
    public function stop () {
        httpClientEndpoint.stop();
    }

};

@Description {value:"Object representing the WebSub Hub Client Endpoint configuration"}
@Field {value:"url: The URL of the target Hub"}
public type HubClientEndpointConfiguration {
    string url,
    http:SecureSocket? secureSocket,
    //TODO: include header, topic-resource map
};
