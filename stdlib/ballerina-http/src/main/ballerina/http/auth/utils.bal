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


documentation {
    Authentication header name.
}
@final string AUTH_HEADER = "Authorization";
documentation {
    Basic authentication scheme.
}
@final string AUTH_SCHEME_BASIC = "Basic";
documentation {
    Bearer authentication scheme.
}
@final string AUTH_SCHEME_BEARER = "Bearer";
documentation {
    Auth annotation package.
}
@final string ANN_PACKAGE = "ballerina/http";
documentation {
    Resource level annotation name.
}
@final string RESOURCE_ANN_NAME = "ResourceConfig";
documentation {
    Service level annotation name.
}
@final string SERVICE_ANN_NAME = "ServiceConfig";
documentation {
    Auth provider config name.
}
@final string AUTH_PROVIDER_CONFIG = "config";
documentation {
    LDAP auth provider config name.
}
@final string AUTH_PROVIDER_LDAP = "ldap";
documentation {
    JDBC auth provider config name.
}
@final string AUTH_PROVIDER_JDBC = "jdbc";
documentation {
    AD auth provider config name.
}
@final string AUTH_PROVIDER_AD = "activeDirectory";

documentation {
    Authn scheme basic.
}
@final string AUTHN_SCHEME_BASIC = "basic";
documentation {
    Authn scheme JWT.
}
@final string AUTH_SCHEME_JWT = "jwt";
documentation {
    Authn scheme OAuth2.
}
@final string AUTH_SCHEME_OAUTH2 = "oauth2";

documentation {
    Extracts the basic authentication header value from the request.

    P{{req}} Request instance
    R{{}} Value of the basic authentication header, or nil if not found
}
public function extractBasicAuthHeaderValue(Request req) returns (string|()) {
    // extract authorization header
    try {
        return req.getHeader(AUTH_HEADER);
    } catch (error e) {
        log:printDebug("Error in retrieving header " + AUTH_HEADER + ": " + e.message);
    }
    return ();
}

documentation {
    Error handler.

    P{{message}} Error message
    R{{}} Error populated with the message
}
function handleError(string message) returns (error) {
    error e = {message: message};
    return e;
}
