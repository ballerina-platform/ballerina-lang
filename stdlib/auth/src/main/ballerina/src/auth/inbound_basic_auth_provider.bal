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

import ballerina/config;
import ballerina/crypto;
import ballerina/encoding;
import ballerina/internal;
import ballerina/runtime;

# Represents an inbound basic Auth provider, which is a configuration-file-based Auth store provider.
#
# + basicAuthConfig - The Basic Auth provider configurations.
public type InboundBasicAuthProvider object {

    *InboundAuthProvider;

    public BasicAuthConfig basicAuthConfig;

    # Provides authentication based on the provided configuration.
    #
    # + basicAuthConfig - The Basic Auth provider configurations.
    public function __init(BasicAuthConfig? basicAuthConfig = ()) {
        if (basicAuthConfig is BasicAuthConfig) {
            self.basicAuthConfig = basicAuthConfig;
        } else {
            self.basicAuthConfig = { tableName: CONFIG_USER_SECTION };
        }
    }

    # Attempts to authenticate with credentials.
    #
    # + credential - Credential
    # + return - `true` if authentication is successful, otherwise `false` or `Error` occurred while extracting credentials
    public function authenticate(string credential) returns boolean|Error {
        if (credential == EMPTY_STRING) {
            return false;
        }
        string username;
        string password;
        [username, password] = check extractUsernameAndPassword(credential);
        string passwordFromConfig = readPassword(username);
        boolean authenticated = false;
        // This check is added to avoid having to go through multiple condition evaluations, when value is plain text.
        if (internal:hasPrefix(passwordFromConfig, CONFIG_PREFIX)) {
            if (internal:hasPrefix(passwordFromConfig, CONFIG_PREFIX_SHA256)) {
                authenticated = internal:equalsIgnoreCase(
                                encoding:encodeHex(crypto:hashSha256(internal:toByteArray(password, DEFAULT_CHARSET))),
                                extractHash(passwordFromConfig));
            } else if (internal:hasPrefix(passwordFromConfig, CONFIG_PREFIX_SHA384)) {
                authenticated = internal:equalsIgnoreCase(
                                encoding:encodeHex(crypto:hashSha384(internal:toByteArray(password, DEFAULT_CHARSET))),
                                extractHash(passwordFromConfig));
            } else if (internal:hasPrefix(passwordFromConfig, CONFIG_PREFIX_SHA512)) {
                authenticated = internal:equalsIgnoreCase(
                                encoding:encodeHex(crypto:hashSha512(internal:toByteArray(password, DEFAULT_CHARSET))),
                                extractHash(passwordFromConfig));
            } else {
                authenticated = password == passwordFromConfig;
            }
        } else {
            authenticated = password == passwordFromConfig;
        }
        if (authenticated) {
            setAuthenticationContext("basic", credential);
            setPrincipal(username, self.basicAuthConfig.tableName);
        }
        return authenticated;
    }
};

# The `BasicAuthConfig` record can be used to configure inbound Basic Authentication configurations.
#
# + tableName - The table name of the TOML file of the user-store.
public type BasicAuthConfig record {|
    string tableName;
|};

# Reads the scope(s) of the user of the given username.
#
# + username - The username of the user.
# + tableName - The table name of the TOML file of the user-store.
# + return - An array of groups of the user who is denoted by the username.
function getScopes(string username, string tableName) returns string[] {
    // First, reads the user ID from the user->id mapping.
    // Reads the groups of the user-id.
    return getArray(getConfigAuthValue(tableName + "." + username, "scopes"));
}

# Extracts the password hash from the configuration file.
#
# + configValue - Config value to extract the password from
# + return - Password hash extracted from the configuration field
function extractHash(string configValue) returns string {
    return configValue.substring((<int> configValue.indexOf("{")) + 1, internal:lastIndexOf(configValue, "}"));
}

# Reads the password hash for a user.
#
# + username - Username
# + return - Password hash read from userstore, or nil if not found
function readPassword(string username) returns string {
    // first read the user id from user->id mapping
    // read the hashed password from the user-store file, using the user id
    return getConfigAuthValue(CONFIG_USER_SECTION + "." + username, "password");
}

function getConfigAuthValue(string instanceId, string property) returns string {
    return config:getAsString(instanceId + "." + property, "");
}

# Construct an array of groups from the comma separed group string passed.
#
# + groupString - Comma separated string of groups
# + return - Array of groups, nil if the groups string is empty/nil
function getArray(string groupString) returns string[] {
    string[] groupsArr = [];
    if (groupString.length() == 0) {
        return groupsArr;
    }
    return internal:split(groupString, ",");
}

function setPrincipal(string username, string tableName) {
    runtime:Principal? principal = runtime:getInvocationContext()?.principal;
    if (principal is runtime:Principal) {
        principal.userId = username;
        principal.username = username;
        principal.scopes = getScopes(username, tableName);
    }
}
