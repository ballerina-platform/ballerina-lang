import ballerina/http;
import ballerina/config;
import ballerina/log;
import ballerina/ldap;

// Defines the LDAP connection configurations.
ldap:LdapConnectionConfig ldapConfig = {
    domainName: "ballerina.io",
    connectionURL: "ldap://localhost:9095",
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

// Creates an inbound LDAP authentication provider with the LDAP
// connection configurations.
ldap:InboundLdapAuthProvider ldapAuthProvider = new(ldapConfig, "ldap01");

// Creates a Basic Auth handler with the created LDAP Auth provider.
http:BasicAuthHandler ldapAuthHandler = new(ldapAuthProvider);

// The endpoint used here is the `http:Listener`. The LDAP Auth handler is
// set to this endpoint using the `authHandlers` attribute.
// It is optional to override the authentication and authorization at the
// service and resource levels.
listener http:Listener ep = new (9090, {
    auth: {
        authHandlers: [ldapAuthHandler]
    },
    // The secure hello world sample uses HTTPS.
    secureSocket: {
        keyStore: {
            path: config:getAsString("b7a.home") +
                  "/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

service hello on ep {
    resource function sayHello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", result);
        }
    }
}
