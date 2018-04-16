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
package ballerina.h2;

import ballerina/sql;

@Description {value:"Represents an H2 client endpoint"}
@Field {value:"epName: The name of the endpoint"}
@Field {value:"config: The configurations associated with the endpoint"}
public type Client object {
    public {
        string epName;
        ClientEndpointConfiguration config;
        sql:SQLClient h2Client;
    }

    @Description {value:"Gets called when the endpoint is being initialized during the package initialization."}
    public function init(ClientEndpointConfiguration config);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getClient() returns sql:SQLClient {
        return self.h2Client;
    }

    @Description {value:"Stops the registered service"}
    @Return {value:"Error occured during registration"}
    public function stop() {
    }
};


public type ClientEndpointConfiguration {
    string host = "",
    string path = "",
    int port = 0,
    string name = "",
    string username = "",
    string password = "",
    sql:PoolOptions poolOptions,
    map | () dbOptions,
};

public native function createClient(ClientEndpointConfiguration config) returns sql:SQLClient;

public function Client::init(ClientEndpointConfiguration config) {
    self.h2Client = createClient(config);
}
