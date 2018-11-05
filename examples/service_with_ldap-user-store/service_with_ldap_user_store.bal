import ballerina/auth;
import ballerina/http;

// Configuration options to integrate Ballerina service
// with an LDAP server
auth:LdapAuthProviderConfig ldapAuthProviderConfig = {
    // Unique name to identify the user store
    domainName: "ballerina.io",
    // Connection URL to the LDAP server
    connectionURL: "ldap://localhost:9389",
    // The username used to connect to the LDAP server
    connectionName: "uid=admin,ou=system",
    // Password for the ConnectionName user
    connectionPassword: "secret",
    // DN of the context or object under which
    // the user entries are stored in the LDAP server
    userSearchBase: "ou=Users,dc=ballerina,dc=io",
    // Object class used to construct user entries
    userEntryObjectClass: "identityPerson",
    // The attribute used for uniquely identifying a user entry
    userNameAttribute: "uid",
    // Filtering criteria used to search for a particular user entry
    userNameSearchFilter: "(&(objectClass=person)(uid=?))",
    // Filtering criteria for searching user entries in the LDAP server
    userNameListFilter: "(objectClass=person)",
    // DN of the context or object under which
    // the group entries are stored in the LDAP server
    groupSearchBase: ["ou=Groups,dc=ballerina,dc=io"],
    // Object class used to construct group entries
    groupEntryObjectClass: "groupOfNames",
    // The attribute used for uniquely identifying a group entry
    groupNameAttribute: "cn",
    // Filtering criteria used to search for a particular group entry
    groupNameSearchFilter: "(&(objectClass=groupOfNames)(cn=?))",
    // Filtering criteria for searching group entries in the LDAP server
    groupNameListFilter: "(objectClass=groupOfNames)",
    // Define the attribute that contains the distinguished names
    // (DN) of user objects that are in a group
    membershipAttribute: "member",
    // To indicate whether to cache the role list of a user
    userRolesCacheEnabled: true,
    // Define whether LDAP connection pooling is enabled
    connectionPoolingEnabled: false,
    // Timeout in making the initial LDAP connection
    ldapConnectionTimeout: 5000,
    // The value of this property is the read timeout in
    // milliseconds for LDAP operations
    readTimeout: 60000,
    // Retry the authentication request if a timeout happened
    retryAttempts: 3
};

http:AuthProvider basicAuthProvider = {
    id: "basic01",
    scheme: http:AUTHN_SCHEME_BASIC,
    authStoreProvider: http:AUTH_PROVIDER_LDAP,
    authStoreProviderConfig: ldapAuthProviderConfig
};

// The endpoint used here is `http:SecureListener`, which by default tries to
// authenticate and authorize each request. The developer has the option to
// override the authentication and authorization at the service level and
// resource level.
endpoint http:SecureListener ep {
    port: 9090,
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
// Auth configuration comprises of two parts - authentication & authorization.
// Authentication can be enabled by setting the `authentication:{enabled:true}`
// annotation attribute.
service<http:Service> helloService bind ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resourceOne",
        authConfig: {
            scopes: ["admin"]
        }
    }
    // The authentication and authorization settings can be overridden at
    // resource level.
    // The enableAuthz resource would inherit the
    // `authentication:{enabled:true}` flag from the service level,
    // and override the scope defined in the service level with test scope.
    resourceOne(endpoint caller, http:Request req) {
        _ = caller->respond("Hello, World!!!");
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resourceTwo",
        authConfig: {
            scopes: ["test"]
        }
    }
    resourceTwo(endpoint caller, http:Request req) {
        _ = caller->respond("Hello, World!!!");
    }
}
