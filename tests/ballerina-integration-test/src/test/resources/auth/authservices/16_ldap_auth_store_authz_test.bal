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

import ballerina/ldap;
import ballerina/http;

ldap:LdapConnectionConfig ldapConfig01 = {
    domainName: "ballerina.io",
    connectionURL: "ldap://localhost:9389",
    connectionName: "uid=admin,ou=system",
    connectionPassword: "secret",
    userSearchBase: "ou=Users,dc=ballerina,dc=io",
    userEntryObjectClass: "identityPerson",
    userNameAttribute: "uid",
    userNameSearchFilter: "(&(objectClass=person)(uid=?))",
    userNameListFilter: "(objectClass=person)",
    groupSearchBase: ["ou=Groups,dc=ballerina,dc=io"],
    groupEntryObjectClass: "groupOfNames",
    groupNameAttribute: "cn",
    groupNameSearchFilter: "(&(objectClass=groupOfNames)(cn=?))",
    groupNameListFilter: "(objectClass=groupOfNames)",
    membershipAttribute: "member",
    userRolesCacheEnabled: true,
    connectionPoolingEnabled: false,
    ldapConnectionTimeout: 5000,
    readTimeout: 60000,
    retryAttempts: 3
};

ldap:InboundLdapAuthProvider ldapAuthProvider01 = new(ldapConfig01, "ldap01");
http:BasicAuthHandler ldapAuthHandler01 = new(ldapAuthProvider01);

listener http:Listener authEP = new(9112, config = {
    auth: {
        authHandlers: [ldapAuthHandler01]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/auth",
    auth: {
        enabled: true
    }
}
service authService on authEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/failAuthz",
        auth: {
            scopes: ["admin", "support"]
        }
    }
    resource function failAuthz(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello, World!!!");
    }
}
