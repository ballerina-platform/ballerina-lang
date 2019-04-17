## Module Overview

This module provides a set of default authentication providers that can be extended to create new authentication providers. 

### Authentication Provider

An authentication provider defines an authentication scheme that could be used to protect endpoints. The `auth:AuthStoreProvider` type acts as the interface for all the authentication providers. Any type of implementation, such as LDAP, JDBC, and file based, should be object-wise similar.

By default, there are three implementations of the `auth:AuthProvider`. They are; the `auth:ConfigAuthStoreProvider`,
 which authenticates based on usernames and passwords stored in a configuration file; the `auth:JWTAuthProvider`, which
 authenticates by validating a JWT; and finally the `auth:LdapAuthStoreProvider`, which authenticates based on the user
 credentials stored in an active directory or an LDAP.

When creating a new authentication provider, there is a function that need to be implemented. 
- `authenticate` : Authenticates the user based on a credential, which can be username/password, or a token such as JWT.

### Config Auth Store Provider

`auth:ConfigAuthStoreProvider` is an implementation of the `auth:AuthProvider` interface, which uses the Ballerina configuration file
 to store usernames, passwords, scopes and the relevant associations.

A user is denoted by a section in the configuration file. The password and the scopes assigned to the user are denoted
 as keys under the relevant user section as seen below. 

 ```
 [b7a.users.<username>]
 password="<password>"
 scopes="<comma_separated_scopes>"
 ```

### LDAP Auth Store Provider

`auth:LdapAuthStoreProvider` is another implementation of the `auth:AuthProvider` interface, which connects to an active directory or an LDAP to retrieve the necessary user information to perform authentication and authorization.

### JWT Auth Provider

`auth:JWTAuthProvider` is another implementation of the `auth:AuthProvider` interface, which authenticate by validating a JWT.
