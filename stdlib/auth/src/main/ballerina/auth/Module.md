## Module Overview

This module provides a set of default authentication provider configurations that can be extended to create new authentication providers. 

### Authentication Provider

An authentication provider defines an authentication scheme that could be used to authenticate endpoints. The `auth:AuthStoreProvider` acts as the interface for all the authentication providers. Any type of implementation such as LDAP, JDBC, and file-based should be object-equivalent.

 credentials stored in an active directory or an LDAP.

When creating a new authentication provider, there is a function that needs to be implemented. 
- `authenticate` : Authenticates the user based on a credential, which can be username/password, or a token such as JWT.

### Config Auth Store Provider

The `auth:ConfigAuthStoreProvider` is an implementation of the `auth:AuthProvider` interface, which uses the Ballerina configuration file
 to store usernames, passwords, scopes, and relevant associations.

A user is denoted by a section in the configuration file. The password and the scopes assigned to the user are denoted
 as keys under the relevant user section as shown below. 

 ```
 [b7a.users.<username>]
 password="<password>"
 scopes="<comma_separated_scopes>"
 ```

### LDAP Auth Store Provider

The `auth:LdapAuthStoreProvider` is another implementation of the `auth:AuthProvider` interface. This connects to an active directory or an LDAP, retrieves the necessary user information, and performs authentication and authorization.

### JWT Auth Provider

The `auth:JWTAuthProvider` is another implementation of the `auth:AuthProvider` interface, which authenticates by validating a JWT.
