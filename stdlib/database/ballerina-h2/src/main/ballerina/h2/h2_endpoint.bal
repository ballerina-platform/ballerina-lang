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

documentation {
    The Client endpoint configuration for h2 databases.

    F{{host}} - The host name of the database to connect (in case of server baased DB).
    F{{path}} - The path of the database connection (in case of file baased DB).
    F{{port}} - The port of the database to connect (in case of server baased DB).
    F{{name}} - The name of the database to connect.
    F{{username}} - Username for the database connection.
    F{{password}} - Password for the database connection.
    F{{poolOptions}} - Properties for the connection pool configuration.
    F{{dbOptions}} - DB specific properties.
}
public type ClientEndpointConfiguration {
    string host = "",
    string path = "",
    int port = 0,
    string name = "",
    string username = "",
    string password = "",
    sql:PoolOptions poolOptions,
    map dbOptions,
};


documentation {
    Represents an H2 client endpoint.

    F{{config}} - The configurations associated with the SQL endpoint.
}

public type Client object {
    public {
        ClientEndpointConfiguration config;
        sql:CallerActions h2Client;
    }

    documentation {
        Gets called when the endpoint is being initialized during the package initialization.

        P{{config}} - he ClientEndpointConfiguration of the endpoint.
    }
    public function init(ClientEndpointConfiguration config) {
        self.h2Client = createClient(config);
    }

    documentation {
        Returns the connector that the client code uses.
    }
    public function getCallerActions() returns sql:CallerActions {
        return self.h2Client;
    }
};

native function createClient(ClientEndpointConfiguration config) returns sql:CallerActions;
