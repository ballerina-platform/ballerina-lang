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


@Description {value:"Authentication header name"}
@final string AUTH_HEADER = "Authorization";
@Description {value:"Basic authentication scheme"}
@final string AUTH_SCHEME_BASIC = "Basic";
@Description {value:"Bearer authentication scheme"}
@final string AUTH_SCHEME_BEARER = "Bearer";
@Description {value:"Auth annotation package"}
@final string ANN_PACKAGE = "ballerina.http";
@Description {value:"Resource level annotation name"}
@final string RESOURCE_ANN_NAME = "ResourceConfig";
@Description {value:"Service level annotation name"}
@final string SERVICE_ANN_NAME = "ServiceConfig";
@Description {value:"Auth provider config name"}
@final string AUTH_PROVIDER_CONFIG = "config";
@Description {value:"ldap auth provider config name"}
@final string AUTH_PROVIDER_LDAP = "ldap";
@Description {value:"jdbc auth provider config name"}
@final string AUTH_PROVIDER_JDBC = "jdbc";
@Description {value:"AD auth provider config name"}
@final string AUTH_PROVIDER_AD = "activeDirectory";

@Description {value:"Authn scheme basic"}
@final string AUTHN_SCHEME_BASIC = "basic";
@Description {value:"Authn scheme JWT"}
@final string AUTH_SCHEME_JWT = "jwt";
@Description {value:"Authn scheme OAuth2"}
@final string AUTH_SCHEME_OAUTH2 = "oauth2";

@Description {value:"Extracts the basic authentication header value from the request"}
@Param {value:"req: Request instance"}
@Return {value:"string: value of the basic authentication header, or null if not found"}
public function extractBasicAuthHeaderValue (Request req) returns (string|()) {
    // extract authorization header
    try {
        return req.getHeader(AUTH_HEADER);
    } catch (error e) {
        log:printDebug("Error in retrieving header " + AUTH_HEADER + ": " + e.message);
    }
    return ();
}

@Description {value:"Error handler"}
@Param {value:"message: error message"}
@Return {value:"error: error populated with the message"}
function handleError (string message) returns (error) {
    error e = {message:message};
    return e;
}
