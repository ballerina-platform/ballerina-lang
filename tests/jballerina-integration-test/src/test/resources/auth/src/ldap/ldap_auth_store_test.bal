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
import ballerina/ldap;
import ballerina/http;

ldap:LdapConnectionConfig ldapConfig = {
    domainName: "ballerina.io",
    connectionURL: "ldap://localhost:20399",
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
    connectionTimeoutInMillis: 5000,
    readTimeoutInMillis: 60000,
    retryAttempts: 3
};

ldap:InboundLdapAuthProvider ldapAuthProvider = new(ldapConfig, "ldap01");
http:BasicAuthHandler ldapAuthHandler = new(ldapAuthProvider);

listener http:Listener ep = new(20300, {
    auth: {
        authHandlers: [ldapAuthHandler]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/ldapAuth",
    auth: {
        enabled: true
    }
}
service helloService on ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/disableAuthz"
    }
    resource function disableAuthz(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello, World!!!");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/enableAuthz",
        auth: {
            scopes: ["test"]
        }
    }
    resource function enableAuthz(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello, World!!!");
    }

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
