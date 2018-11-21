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
import ballerina/runtime;

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
# + ldapConnectionTimeout - Timeout in making the initial LDAP connection
# + readTimeout - The value of this property is the read timeout in milliseconds for LDAP operations
# + retryAttempts - Retry the authentication request if a timeout happened
# + secureClientSocket - The SSL configurations for the ldap client socket. This needs to be configured in order to
#                  communicate through ldaps.
public type LdapAuthProviderConfig record {
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
    int ldapConnectionTimeout = 5000;
    int readTimeout = 60000;
    int retryAttempts = 0;
    SecureClientSocket? secureClientSocket = ();
    !...
};

# Configures the SSL/TLS options to be used for LDAP communication.
#
# + trustStore - Configures the trust store to be used
# + trustedCertFile - A file containing a list of certificates or a single certificate that the client trusts
public type SecureClientSocket record {
    TrustStore? trustStore;
    string trustedCertFile;
    !...
};

# A record for providing trust store related configurations.
#
# + path - Path to the trust store file
# + password - Trust store password
public type TrustStore record {
    string path;
    string password;
    !...
};

# Represents Ballerina configuration for LDAP based auth store provider
#
# + ldapAuthProviderConfig - LDAP auth store configurations
# + instanceId - Endpoint instance id
public type LdapAuthStoreProvider object {

    public LdapAuthProviderConfig ldapAuthProviderConfig;
    public string instanceId = "";

    # Create an LDAP auth store with the given configurations.
    #
    # + ldapAuthProviderConfig -  LDAP auth store configurations
    # + instanceId - Endpoint instance id
    public new (ldapAuthProviderConfig, instanceId) {
        initLdapConnectionContext(self, instanceId);
    }

    # Authenticate with username and password
    #
    # + username - user name
    # + password - password
    # + return - true if authentication is a success, else false
    public function authenticate(string username, string password) returns boolean {
        boolean isAuthenticated = self.doAuthenticate(username, password);
        if (isAuthenticated) {
            runtime:UserPrincipal userPrincipal = runtime:getInvocationContext().userPrincipal;
            userPrincipal.userId = self.ldapAuthProviderConfig.domainName + ":" + username;
            // By default set userId as username.
            userPrincipal.username = username;
        }
        return isAuthenticated;
    }

    # Reads the scope(s) for the user with the given username
    #
    # + username - username
    # + return - array of groups for the user denoted by the username
    public function getScopes(string username) returns string[] {
        // Reads the groups for the username
        return self.getScopesOfUser(username);
    }

    # Authenticate with username and password
    #
    # + username - user name
    # + password - password
    # + return - true if authentication is a success, else false
    public extern function doAuthenticate(string username, string password) returns boolean;

    # Reads the scope(s) for the user with the given username
    #
    # + username - username
    # + return - array of groups for the user denoted by the username
    public extern function getScopesOfUser(string username) returns string[];
};

# Initailizes LDAP connection context
#
# + ldapAuthStoreProvider - LdapAuthStoreProvider provider object
# + instanceId - Unique id generated to identify an endpoint
public extern function initLdapConnectionContext(LdapAuthStoreProvider ldapAuthStoreProvider, string instanceId);
