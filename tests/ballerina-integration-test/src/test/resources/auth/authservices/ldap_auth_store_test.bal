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
import ballerina/http;

auth:LdapAuthProviderConfig ldapAuthProviderConfig = {
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

http:AuthProvider basicAuthProvider = {
    id: "basic01",
    scheme: http:AUTHN_SCHEME_BASIC,
    authStoreProvider: http:AUTH_PROVIDER_LDAP,
    authStoreProviderConfig: ldapAuthProviderConfig
};

endpoint http:Listener ep {
    port: 9096,
    authProviders: [basicAuthProvider],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath: "/ldapAuth",
    authConfig: {
        authentication: { enabled: true }
    }
}
service<http:Service> helloService bind ep {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/disableAuthz"
    }
    disableAuthz(endpoint caller, http:Request req) {
        _ = caller->respond("Hello, World!!!");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/enableAuthz",
        authConfig: {
            scopes: ["test"]
        }
    }
    enableAuthz(endpoint caller, http:Request req) {
        _ = caller->respond("Hello, World!!!");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/failAuthz",
        authConfig: {
            scopes: ["admin", "support"]
        }
    }
    failAuthz(endpoint caller, http:Request req) {
        _ = caller->respond("Hello, World!!!");
    }
}
