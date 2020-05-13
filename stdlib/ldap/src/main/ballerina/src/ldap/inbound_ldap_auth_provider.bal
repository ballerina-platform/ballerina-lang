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

import ballerina/auth;
import ballerina/crypto;
import ballerina/java;

# Represents the inbound LDAP auth provider. This connects to an active directory or an LDAP, retrieves the necessary
# user information, and performs authentication and authorization.
# The `ldap:InboundLdapAuthProvider` is another implementation of the `auth:InboundAuthProvider` interface.
# ```ballerina
# ldap:InboundLdapAuthProvider inboundLdapAuthProvider = new(ldapConfig, "instanceId");
# ```
#
# + instanceId - Instance ID of the endpoint
# + ldapConnection - LDAP connection instance
# + ldapConnectionConfig - LDAP connection configurations
public type InboundLdapAuthProvider object {

    *auth:InboundAuthProvider;

    public string instanceId;
    public LdapConnection ldapConnection;
    public LdapConnectionConfig ldapConnectionConfig;

    # Creates an LDAP auth store with the given configurations.
    #
    # + ldapConnectionConfig - The `ldap:LdapConnectionConfig` instance
    # + instanceId - Instance ID of the endpoint
    public function __init(LdapConnectionConfig ldapConnectionConfig, string instanceId) {
        self.instanceId = instanceId;
        self.ldapConnectionConfig = ldapConnectionConfig;
        LdapConnection|Error ldapConnection = initLdapConnectionContext(self.ldapConnectionConfig, instanceId);
        if (ldapConnection is LdapConnection) {
            self.ldapConnection = ldapConnection;
        } else {
            panic ldapConnection;
        }
    }

# Authenticates the base64-encoded `username:password` credentials.
# ```ballerina
# boolean|auth:Error result = inboundLdapAuthProvider.authenticate("<credential>");
# ```
#
# + credential - Base64-encoded `username:password` value
# + return - `true` if authentication is successful, `false` otherwise, or else an `auth:Error` occurred while
#            authenticating the credentials
    public function authenticate(string credential) returns boolean|auth:Error {
        if (credential == "") {
            return false;
        }
        [string, string] [username, password] = check auth:extractUsernameAndPassword(credential);
        boolean|Error authenticated = doAuthenticate(self.ldapConnection, username, password);
        string[]|Error groups = getGroups(self.ldapConnection, username);
        string[] scopes;
        if (groups is string[]) {
            scopes = groups;
        } else {
            return prepareAuthError("Failed to get groups from LDAP with the username: " + username, groups);
        }
        if (authenticated is boolean) {
            if (authenticated) {
                auth:setAuthenticationContext("ldap", credential);
                string userId = self.ldapConnectionConfig.domainName + ":" + username;
                auth:setPrincipal(userId, username, scopes);
            }
            return authenticated;
        } else {
            return prepareAuthError("Failed to authenticate LDAP with username: " + username, authenticated);
        }
    }
};

# Represents the configurations that are required for an LDAP auth store.
#
# + domainName - Unique name to identify the user store
# + connectionURL - Connection URL of the LDAP server
# + connectionName - The username used to connect to the LDAP server
# + connectionPassword - The password used to connect to the LDAP server
# + userSearchBase - DN of the context or object under which the user entries are stored in the LDAP server
# + userEntryObjectClass - Object class used to construct user entries
# + userNameAttribute - The attribute used for uniquely identifying a user entry
# + userNameSearchFilter - Filtering criteria used to search for a particular user entry
# + userNameListFilter - Filtering criteria for searching user entries in the LDAP server
# + groupSearchBase - DN of the context or object under which the group entries are stored in the LDAP server
# + groupEntryObjectClass - Object class used to construct group entries
# + groupNameAttribute - The attribute used for uniquely identifying a group entry
# + groupNameSearchFilter - Filtering criteria used to search for a particular group entry
# + groupNameListFilter - Filtering criteria for searching group entries in the LDAP server
# + membershipAttribute - Define the attribute, which contains the distinguished names (DN) of user objects that are there in a group
# + userRolesCacheEnabled -  To indicate whether to cache the role list of a user
# + connectionPoolingEnabled - Define whether LDAP connection pooling is enabled
# + connectionTimeoutInMillis - Timeout (in milliseconds) in making the initial LDAP connection 
# + readTimeoutInMillis - Reading timeout in milliseconds for LDAP operations
# + retryAttempts - Retry the authentication request if a timeout happened
# + secureSocket - The SSL configurations for the LDAP client socket. This needs to be configured in order to
#                  communicate through LDAPs
public type LdapConnectionConfig record {|
    string domainName;
    string connectionURL;
    string connectionName;
    string connectionPassword;
    string userSearchBase;
    string userEntryObjectClass;
    string userNameAttribute;
    string userNameSearchFilter;
    string userNameListFilter;
    string[] groupSearchBase;
    string groupEntryObjectClass;
    string groupNameAttribute;
    string groupNameSearchFilter;
    string groupNameListFilter;
    string membershipAttribute;
    boolean userRolesCacheEnabled = false;
    boolean connectionPoolingEnabled = true;
    int connectionTimeoutInMillis = 5000;
    int readTimeoutInMillis = 60000;
    int retryAttempts = 0;
    SecureSocket secureSocket?;
|};

# Configures the SSL/TLS options to be used for LDAP communication.
#
# + trustStore - Configures the trust store to be used
# + trustedCertFile - A file containing the certificate(s), which the client trusts
public type SecureSocket record {|
    crypto:TrustStore trustStore?;
    string trustedCertFile?;
|};

# Represets the LDAP connection.
public type LdapConnection record {|
|};

# Retrieves the group(s) of the user related to the provided username.
# ```ballerina
# string[]|ldap:Error groups = ldap:getGroups(ldapConnection, username);
# ```
#
# + ldapConnection - The `ldap:LdapConnection` instance
# + username - Username of the user to be checked for the groups
# + return - Array of groups of the provided user or else an `ldap:Error` if it fails
public function getGroups(LdapConnection ldapConnection, string username) returns string[]|Error {
    return externGetGroups(ldapConnection, java:fromString(username));
}

function externGetGroups(LdapConnection ldapConnection, handle username) returns string[]|Error = @java:Method {
    name: "getGroups",
    class: "org.ballerinalang.stdlib.ldap.nativeimpl.GetGroups"
} external;

# Authenticates with the username and password.
# ```ballerina
# boolean|ldap:Error result = ldap:doAuthenticate(ldapConnection, username, password);
# ```
#
# + ldapConnection - The `ldap:LdapConnection` instance
# + username - Username of the user to be authenticated
# + password - Password of the user to be authenticated
# + return - `true` if authentication is successful, `false` otherwise, or else an `ldap:Error` if an error occurred
public function doAuthenticate(LdapConnection ldapConnection, string username, string password) returns boolean|Error {
    return externDoAuthenticate(ldapConnection, java:fromString(username), java:fromString(password));
}

public function externDoAuthenticate(LdapConnection ldapConnection, handle username, handle password)
                                     returns boolean|Error = @java:Method {
    name: "doAuthenticate",
    class: "org.ballerinalang.stdlib.ldap.nativeimpl.Authenticate"
} external;

# Initailizes the LDAP connection context.
# ```ballerina
# ldap:LdapConnection|ldap:Error connection = ldap:initLdapConnectionContext(ldapConnectionConfig, instanceId);
# ```
#
# + ldapConnectionConfig - The `ldap:LdapConnectionConfig` instance
# + instanceId - Instance ID of the endpoint
# + return - The `ldap:LdapConnection` instance or else an `ldap:Error` if an error occurred
public function initLdapConnectionContext(LdapConnectionConfig ldapConnectionConfig, string instanceId)
                                          returns LdapConnection|Error {
    return externInitLdapConnectionContext(ldapConnectionConfig, java:fromString(instanceId));
}

public function externInitLdapConnectionContext(LdapConnectionConfig ldapConnectionConfig, handle instanceId)
                                                returns LdapConnection|Error = @java:Method {
    name: "initLdapConnectionContext",
    class: "org.ballerinalang.stdlib.ldap.nativeimpl.InitLdapConnectionContext"
} external;
