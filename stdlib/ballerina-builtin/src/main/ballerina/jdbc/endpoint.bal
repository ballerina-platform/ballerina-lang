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

package ballerina.jdbc;

import ballerina/sql;

documentation {
    Represents an JDBC SQL client endpoint.

    F{{config}} - The configurations associated with the SQL endpoint.
}
public type Client object {
    private {
        sql:ClientEndpointConfiguration config;
        sql:CallerActions callerActions;
    }

    documentation {
        Gets called when the endpoint is being initialized during the package initialization.

        P{{config}} - he ClientEndpointConfiguration of the endpoint.
    }
    function init(sql:ClientEndpointConfiguration config) {
        self.callerActions = sql:createSQLClient(config);
    }

    documentation {
        Returns the connector that the client code uses.
    }
    function getClient() returns sql:CallerActions {
        return self.callerActions;
    }
};
