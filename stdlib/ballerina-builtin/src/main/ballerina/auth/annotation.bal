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

@Description {value:"Representation of Auth Config"}
@Field {value:"authentication: Authentication struct instance"}
@Field {value:"scope: scope name"}
public struct AuthConfig {
    Authentication|null authentication;
    string scope;
}

@Description {value:"Representation of Authentation Config"}
@Field {value:"enabled: flag to enable/disable authentication"}
public struct Authentication {
    boolean enabled;
}

public function <AuthConfig authConfig> AuthConfig() {
    authConfig.authentication = null;
    authConfig.scope = "";
}

@Description {value:"Authentication config annotation for a Service"}
public annotation <service,resource> Config AuthConfig;
