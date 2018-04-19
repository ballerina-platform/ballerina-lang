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

package ballerina.sql;

documentation {
    Represents an SQL client endpoint.

    F{{config}} - The configurations associated with the SQL endpoint.
}
public type Client object {
    public {
        ClientEndpointConfiguration config;
        CallerActions callerActions;
    }

    documentation {
        Gets called when the endpoint is being initialized during the package initialization.

        P{{config}} - he ClientEndpointConfiguration of the endpoint.
    }
    public function init(ClientEndpointConfiguration config) {
        self.callerActions = createSQLClient(config);
    }

    documentation {
        Returns the connector that the client code uses.
    }
    public function getCallerActions() returns CallerActions {
        return self.callerActions;
    }
};

native function createSQLClient(ClientEndpointConfiguration config) returns CallerActions;
