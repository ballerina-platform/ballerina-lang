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
    groupSearchBase: "ou=Groups,dc=ballerina,dc=io",
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
    id: "basic1",
    scheme: "basic",
    authStoreProvider: "ldap",
    authStoreProviderConfig: ldapAuthProviderConfig
};

endpoint http:SecureListener ep {
    port: 9096,
    authProviders: [basicAuthProvider]
};

@http:ServiceConfig {
    basePath: "/hello",
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
