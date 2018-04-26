## Package Overview

This package provides a set of default authentication providers that can be extended to create new authentication providers. 

### Authentication Provider

An authentication provider defines an authentication scheme that could be used to protect endpoints. The `auth:AuthProvider` type acts as the interface for all the authentication providers. Any type of implementation, such as LDAP, JDBC, and file based, should be object-wise similar. 

By default there are two implementations of that. They are, `auth:ConfigAuthProvider` which authenticates based on usernames and passwords stored in ‘ballerina.conf’, and `auth:JWTAuthProvider` which authenticates by validating a JWT. It is possible to implement more such authentication mechanisms.

When creating a new authentication provider, there are two functions that need to be implemented. 
- `authenticate` : authenticate the user based on a credential, which can be username/password or a token such as JWT.
- `getScopes` : provide the scopes associated with the user. Scopes are primarily permissions that are required to access a protected resource. 

### Config Auth Provider

`ConfigAuthProvider` is an implementation of the `AuthProvider` interface, which uses the Ballerina configuration file
 to store usernames, passwords, scopes and the relevant associations.

A user is denoted by a section in the configuration file. The password and the scopes assigned to the user are denoted
 as keys under the relevant user section, as follows:

 ```
 [b7a.users.<username>]
 password="<password>"
 scopes="<comma_separated_scopes>"
 ```
