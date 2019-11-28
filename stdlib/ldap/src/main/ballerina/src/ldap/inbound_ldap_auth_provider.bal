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

# Represents Ballerina configuration for LDAP based auth provider.
#
# + instanceId - Endpoint instance id
# + ldapConnection - LDAP connection
# + ldapConnectionConfig - LDAP connection configurations
public type InboundLdapAuthProvider object {

    *auth:InboundAuthProvider;

    public string instanceId;
    public LdapConnection ldapConnection;
    public LdapConnectionConfig ldapConnectionConfig;

    # Create an LDAP auth store with the given configurations.
    #
    # + ldapConnectionConfig - LDAP connection configurations
    # + instanceId - Endpoint instance id
    public function __init(LdapConnectionConfig ldapConnectionConfig, string instanceId) {
        self.instanceId = instanceId;
        self.ldapConnectionConfig = ldapConnectionConfig;
        var ldapConnection = initLdapConnectionContext(self.ldapConnectionConfig, instanceId);
        if (ldapConnection is LdapConnection) {
            self.ldapConnection = ldapConnection;
        } else {
            panic ldapConnection;
        }
    }

    # Authenticate with username and password.
    #
    # + credential - Credential value
    # + return - `true` if authentication is successful, otherwise `false` or `auth:Error` occurred while extracting credentials
    public function authenticate(string credential) returns boolean|auth:Error {
        if (credential == "") {
            return false;
        }
        string username;
        string password;
        string[] scopes;
        [username, password] = check auth:extractUsernameAndPassword(credential);
        var authenticated = doAuthenticate(self.ldapConnection, username, password);
        var groups = getGroups(self.ldapConnection, username);
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
            return prepareAuthError("Failed to authenticate LDAP with username: " + username + " and password: " + password, authenticated);
        }
    }
};

# Represents configurations that required for LDAP auth store.
#
# + domainName - Unique name to identify the user store
# + connectionURL - Connection URL to the LDAP server
# + connectionName - The username used to connect to the LDAP server
# + connectionPassword - Password for the ConnectionName user
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
# + membershipAttribute - Define the attribute that contains the distinguished names (DN) of user objects that are in a group
# + userRolesCacheEnabled -  To indicate whether to cache the role list of a user
# + connectionPoolingEnabled - Define whether LDAP connection pooling is enabled
# + connectionTimeoutInMillis - Timeout in making the initial LDAP connection in milliseconds
# + readTimeoutInMillis - The value of this property is the read timeout in milliseconds for LDAP operations
# + retryAttempts - Retry the authentication request if a timeout happened
# + secureSocket - The SSL configurations for the ldap client socket. This needs to be configured in order to
#                  communicate through ldaps.
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
# + trustedCertFile - A file containing a list of certificates or a single certificate that the client trusts
public type SecureSocket record {|
    crypto:TrustStore trustStore?;
    string trustedCertFile?;
|};

public type LdapConnection record {|
|};

# Reads the scope(s) for the user with the given username.
#
# + ldapConnection - `LdapConnection` instance
# + username - Username of the user to check the groups
# + return - Array of groups for the user denoted by the username or `Error` if error occurred
public function getGroups(LdapConnection ldapConnection, string username) returns string[]|Error = external;

# Authenticate with username and password.
#
# + ldapConnection - `LdapConnection` instance
# + username - Username of the user to be authenticated
# + password - Password of the user to be authenticated
# + return - true if authentication is a success, else false or `Error` if error occurred
public function doAuthenticate(LdapConnection ldapConnection, string username, string password)
                               returns boolean|Error = external;

# Initailizes LDAP connection context.
#
# + ldapConnectionConfig - `LdapConnectionConfig` instance
# + instanceId - Unique id generated to identify an endpoint
# + return - `LdapConnection` instance, or `Error` if error occurred
public function initLdapConnectionContext(LdapConnectionConfig ldapConnectionConfig, string instanceId)
                                          returns LdapConnection|Error = external;
