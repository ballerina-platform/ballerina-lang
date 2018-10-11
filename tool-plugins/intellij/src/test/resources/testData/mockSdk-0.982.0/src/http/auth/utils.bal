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


# Authentication header name.
@final string AUTH_HEADER = "Authorization";
# Basic authentication scheme.
@final string AUTH_SCHEME_BASIC = "Basic";
# Bearer authentication scheme.
@final string AUTH_SCHEME_BEARER = "Bearer";
# Auth annotation package.
@final string ANN_PACKAGE = "ballerina/http";
# Resource level annotation name.
@final string RESOURCE_ANN_NAME = "ResourceConfig";
# Service level annotation name.
@final string SERVICE_ANN_NAME = "ServiceConfig";
# Auth provider config name.
@final string AUTH_PROVIDER_CONFIG = "config";
# LDAP auth provider config name.
@final string AUTH_PROVIDER_LDAP = "ldap";
# JDBC auth provider config name.
@final string AUTH_PROVIDER_JDBC = "jdbc";
# AD auth provider config name.
@final string AUTH_PROVIDER_AD = "activeDirectory";

# Authn scheme basic.
@final string AUTHN_SCHEME_BASIC = "basic";
# Authn scheme JWT.
@final string AUTH_SCHEME_JWT = "jwt";
# Authn scheme OAuth2.
@final string AUTH_SCHEME_OAUTH2 = "oauth2";

# Extracts the basic authentication header value from the request.
#
# + req - Request instance
# + return - Value of the basic authentication header, or nil if not found
public function extractBasicAuthHeaderValue(Request req) returns (string|()) {
    // extract authorization header
    try {
        return req.getHeader(AUTH_HEADER);
    } catch (error e) {
        log:printDebug("Error in retrieving header " + AUTH_HEADER + ": " + e.message);
    }
    return ();
}

# Error handler.
#
# + message - Error message
# + return - Error populated with the message
function handleError(string message) returns (error) {
    error e = {message: message};
    return e;
}
