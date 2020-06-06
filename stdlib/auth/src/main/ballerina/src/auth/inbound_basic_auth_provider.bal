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
import ballerina/stringutils;

# Represents the configuration file based inbound Basic Auth provider, which is an implementation of the
# `auth:InboundAuthProvider` interface.
# ```ballerina
#  auth:InboundBasicAuthProvider inboundBasicAuthProvider = new;
#  ```
# A user is denoted by a section in the Ballerina configuration file. The password and the scopes assigned to the user
# are denoted as keys under the relevant user section as shown below.
# ```
# [b7a.users.<username>]
# password="<password>"
# scopes="<comma_separated_scopes>"
# ```
#
# + basicAuthConfig - The Basic Auth provider configurations
public type InboundBasicAuthProvider object {

    *InboundAuthProvider;

    public BasicAuthConfig basicAuthConfig;

    # Provides authentication based on the provided configurations.
    #
    # + basicAuthConfig - Basic Auth provider configurations
    public function __init(BasicAuthConfig? basicAuthConfig = ()) {
        if (basicAuthConfig is BasicAuthConfig) {
            self.basicAuthConfig = basicAuthConfig;
        } else {
            self.basicAuthConfig = { tableName: CONFIG_USER_SECTION };
        }
    }

# Attempts to authenticate the base64-encoded `username:password` credentials.
# ```ballerina
# boolean|auth:Error authenticationResult = inboundBasicAuthProvider.authenticate("<credential>");
# ```
#
# + credential - Base64-encoded `username:password` value
# + return - `true` if the authentication is successful, `false` otherwise, or else an `auth:Error` occurred
#             while authenticating the credentials
    public function authenticate(string credential) returns boolean|Error {
        if (credential == "") {
            return false;
        }
        [string, string] [username, password] = check extractUsernameAndPassword(credential);
        string passwordFromConfig = readPassword(username, self.basicAuthConfig.tableName);
        boolean authenticated = false;
        // This check is added to avoid having to go through multiple condition evaluations, when value is plain text.
        if (passwordFromConfig.startsWith(CONFIG_PREFIX)) {
            if (passwordFromConfig.startsWith(CONFIG_PREFIX_SHA256)) {
                authenticated = stringutils:equalsIgnoreCase(
                                crypto:hashSha256(password.toBytes()).toBase16(),
                                extractHash(passwordFromConfig));
            } else if (passwordFromConfig.startsWith(CONFIG_PREFIX_SHA384)) {
                authenticated = stringutils:equalsIgnoreCase(
                                crypto:hashSha384(password.toBytes()).toBase16(),
                                extractHash(passwordFromConfig));
            } else if (passwordFromConfig.startsWith(CONFIG_PREFIX_SHA512)) {
                authenticated = stringutils:equalsIgnoreCase(
                                crypto:hashSha512(password.toBytes()).toBase16(),
                                extractHash(passwordFromConfig));
            } else {
                authenticated = password == passwordFromConfig;
            }
        } else {
            authenticated = password == passwordFromConfig;
        }
        if (authenticated) {
            setAuthenticationContext("basic", credential);
            string[] scopes = getScopes(username, self.basicAuthConfig.tableName);
            setPrincipal(username, username, scopes);
        }
        return authenticated;
    }
};

# Represents the inbound Basic Authentication configurations.
#
# + tableName - The table name specified in the user-store TOML configuration
public type BasicAuthConfig record {|
    string tableName;
|};

# Reads the scope(s) of the user identified by the given username.
#
# + username - Username of the user
# + tableName - Table name specified in the user-store TOML configuration
# + return - An array of scopes of the user identified by the username
function getScopes(string username, string tableName) returns string[] {
    // First, reads the user ID from the user->id mapping.
    // Then, reads the scopes of the user-id.
    return getArray(getConfigAuthValue(tableName + "." + username, "scopes"));
}

# Extracts the password hash from the configuration file.
#
# + configValue - Config value to extract the password from
# + return - Password hash extracted from the configuration field
function extractHash(string configValue) returns string {
    return configValue.substring((<int> configValue.indexOf("{")) + 1, stringutils:lastIndexOf(configValue, "}"));
}

# Reads the password hash of a user.
#
# + username - Username of the user
# + return - Password hash read from the userstore or else `()` if not found
function readPassword(string username, string tableName) returns string {
    // First, reads the user ID from the user->id mapping.
    // Then, reads the hashed password from the user-store file using the user ID.
    return getConfigAuthValue(tableName + "." + username, "password");
}

function getConfigAuthValue(string instanceId, string property) returns string {
    return config:getAsString(instanceId + "." + property, "");
}

# Constructs an array of groups from the given comma-separated string of groups.
#
# + groupString - Comma-separated string of groups
# + return - An array of groups or else `()` if the groups string is empty/`()`
function getArray(string groupString) returns string[] {
    if (groupString.length() == 0) {
        return [];
    }
    return stringutils:split(groupString, ",");
}
