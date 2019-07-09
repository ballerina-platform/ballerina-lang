## Module Overview

This module provides default authentication provider configurations that can be extended to create new authentication providers.

### Inbound Authentication Provider

An inbound authentication provider defines an authentication scheme that could be used to authenticate endpoints. The `auth:InboundAuthProvider` acts as the interface for all the inbound authentication providers. Any type of implementation such as LDAP, JDBC, JWT, OAuth2, and file-based should be object-equivalent.

When creating a new inbound authentication provider, you need to implement the below function.
- `authenticate` : Authenticates the user based on the user credentials (i.e., the username/password) or a token such as JWT or OAuth2.

#### Inbound Basic Auth Provider

The `auth:InboundBasicAuthProvider` authenticates based on usernames and passwords stored in a configuration file. The `auth:InboundBasicAuthProvider` is an implementation of the `auth:InboundAuthProvider` interface, which uses the Ballerina configuration file to read usernames, passwords, scopes, and the relevant associations.

```ballerina
auth:InboundBasicAuthProvider basicAuthProvider = new;
```

A user is denoted by a section in the configuration file. The password and the scopes assigned to the user are denoted as keys under the relevant user section as shown below.

 ```
 [b7a.users.<username>]
 password="<password>"
 scopes="<comma_separated_scopes>"
 ```
### Outbound Authentication Provider

An outbound authentication provider defines an authentication scheme that could be used to authenticate external endpoints. The `auth:OutboundAuthProvider` acts as the interface for all the outbound authentication providers. Any type of implementation such as JDBC, JWT, OAuth2, and file-based should be object-equivalent.

When creating a new outbound authentication provider, you need to implement the below functions.
- `generateToken`: Generates the token for authentication with outbound Auth providers such as JWT and OAuth2.
- `inspect`: Inspects the incoming data and generates the token for authentication as needed. For example, if the incoming data indicates that it needs to regenerate the token because the previously-generated token is invalid, this method will generate it.

#### Outbound Basic Auth Provider

The `auth:OutboundBasicAuthProvider` is used to authenticate external endpoints with the use of usernames and passwords provided in the configurations. The `auth:OutboundBasicAuthProvider` is an implementation of the `auth:OutboundAuthProvider` interface.

```ballerina
auth:OutboundBasicAuthProvider basicAuthProvider = new({
    username: "tom",
    password: "123"
});
```
