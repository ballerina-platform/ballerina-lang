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

package ballerina.auth;

@Description {value:"Represents the auth provider. Any type of implementation, such as ldap, jdbc, file based,
etc. should be object-wise similar"}
public type AuthProvider object {

    @Param {value:"username: user name"}
    @Param {value:"password: password"}
    @Return {value:"boolean: true if authentication is a success, else false"}
    public function authenticate (string username, string password) returns (boolean);

    @Description {value:"Reads the scope(s) for the user with the given username"}
    @Param {value:"username: user name"}
    @Return {value:"string[]: array of groups for the user denoted by the username"}
    public function getScopes (string username) returns (string[]);
};
