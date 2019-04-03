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

# Auth annotation module.
const string ANN_MODULE = "ballerina/http";
# Resource level annotation name.
const string RESOURCE_ANN_NAME = "ResourceConfig";
# Service level annotation name.
const string SERVICE_ANN_NAME = "ServiceConfig";
# Authentication header name.
public const string AUTH_HEADER = "Authorization";
# Basic authentication scheme.
public const string AUTH_SCHEME_BASIC = "Basic";
# Bearer authentication scheme.
public const string AUTH_SCHEME_BEARER = "Bearer";

# Inbound authentication schemes.
public type InboundAuthScheme BASIC_AUTH|JWT_AUTH;

# Outbound authentication schemes.
public type OutboundAuthScheme BASIC_AUTH|OAUTH2|JWT_AUTH;

# Basic authentication scheme.
public const BASIC_AUTH = "BASIC_AUTH";
# OAuth2 authentication scheme.
public const OAUTH2 = "OAUTH2";
# JWT authentication scheme.
public const JWT_AUTH = "JWT_AUTH";

# Authentication storage providers for BasicAuth scheme.
public type AuthStoreProvider CONFIG_AUTH_STORE|LDAP_AUTH_STORE;

# Configuration file based authentication storage.
public const CONFIG_AUTH_STORE = "CONFIG_AUTH_STORE";
# LDAP based authentication storage.
public const LDAP_AUTH_STORE = "LDAP_AUTH_STORE";

# Extracts the basic authentication header value from the request.
#
# + req - Request instance
# + return - Value of the basic authentication header, or nil if not found
public function extractBasicAuthHeaderValue(Request req) returns (string|()) {
    // extract authorization header
    var headerValue = trap req.getHeader(AUTH_HEADER);
    if (headerValue is string) {
        return headerValue;
    } else {
        string reason = headerValue.reason();
        log:printDebug(function () returns string {
            return "Error in retrieving header " + AUTH_HEADER + ": " + reason;
        });
    }
    return ();
}

# Error handler.
#
# + message - Error message
# + return - Error populated with the message
function handleError(string message) returns (error) {
    error e = error(message);
    return e;
}
