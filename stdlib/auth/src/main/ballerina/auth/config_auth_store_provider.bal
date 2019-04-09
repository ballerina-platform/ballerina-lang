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
import ballerina/runtime;

const string CONFIG_USER_SECTION = "b7a.users";

# Represents configurations that required for Config auth store.
public type ConfigAuthStoreProviderConfig record {|
|};

# Represents Ballerina configuration file based auth store provider.
#
# + configAuthProviderConfig - Config auth store configurations
public type ConfigAuthStoreProvider object {

    public ConfigAuthStoreProviderConfig configAuthStoreProviderConfig;

    # Create an Config auth store with the given configurations.
    #
    # + configAuthProviderConfig -  Config auth store configurations
    public function __init(ConfigAuthStoreProviderConfig configAuthStoreProviderConfig) {
        self.configAuthStoreProviderConfig = configAuthStoreProviderConfig;
    }

    # Attempts to authenticate with username and password.
    #
    # + user - user name
    # + password - password
    # + return - true if authentication is a success, else false
    public function authenticate(string user, string password) returns boolean {
        if (user == EMPTY_STRING || password == EMPTY_STRING) {
            return false;
        }
        string passwordFromConfig = self.readPassword(user);
        boolean isAuthenticated = false;
        // This check is added to avoid having to go through multiple condition evaluations, when value is plain text.
        if (passwordFromConfig.hasPrefix(CONFIG_PREFIX)) {
            if (passwordFromConfig.hasPrefix(CONFIG_PREFIX_SHA256)) {
                isAuthenticated = encoding:encodeHex(crypto:hashSha256(password.toByteArray(DEFAULT_CHARSET)))
                                    .equalsIgnoreCase(self.extractHash(passwordFromConfig));
            } else if (passwordFromConfig.hasPrefix(CONFIG_PREFIX_SHA384)) {
                isAuthenticated = encoding:encodeHex(crypto:hashSha384(password.toByteArray(DEFAULT_CHARSET)))
                                    .equalsIgnoreCase(self.extractHash(passwordFromConfig));
            } else if (passwordFromConfig.hasPrefix(CONFIG_PREFIX_SHA512)) {
                isAuthenticated = encoding:encodeHex(crypto:hashSha512(password.toByteArray(DEFAULT_CHARSET)))
                                    .equalsIgnoreCase(self.extractHash(passwordFromConfig));
            } else {
                isAuthenticated = password == passwordFromConfig;
            }
        } else {
            isAuthenticated = password == passwordFromConfig;
        }
        if (isAuthenticated) {
            runtime:Principal principal = runtime:getInvocationContext().principal;
            principal.userId = user;
            principal.username = user;
        }
        return isAuthenticated;
    }

    # Extract password hash from the configuration file.
    #
    # + configValue - config value to extract the password from
    # + return - password hash extracted from the configuration field
    public function extractHash(string configValue) returns string {
        return configValue.substring(configValue.indexOf("{") + 1, configValue.lastIndexOf("}"));
    }

    # Reads the scope(s) for the user with the given username.
    #
    # + username - username
    # + return - array of groups for the user denoted by the username
    public function getScopes(string username) returns string[] {
        // first read the user id from user->id mapping
        // reads the groups for the userid
        return self.getArray(self.getConfigAuthValue(CONFIG_USER_SECTION + "." + username, "scopes"));
    }

    # Reads the password hash for a user.
    #
    # + username - username
    # + return - password hash read from userstore, or nil if not found
    public function readPassword(string username) returns string {
        // first read the user id from user->id mapping
        // read the hashed password from the userstore file, using the user id
        return self.getConfigAuthValue(CONFIG_USER_SECTION + "." + username, "password");
    }

    public function getConfigAuthValue(string instanceId, string property) returns string {
        return config:getAsString(instanceId + "." + property, defaultValue = "");
    }

    # Construct an array of groups from the comma separed group string passed.
    #
    # + groupString - comma separated string of groups
    # + return - array of groups, nil if the groups string is empty/nil
    public function getArray(string groupString) returns (string[]) {
        string[] groupsArr = [];
        if (groupString.length() == 0) {
            return groupsArr;
        }
        return groupString.split(",");
    }
};
