## Module Overview

This module provides default authentication provider configurations that can be extended to create new authentication providers.

### Authentication Provider

An authentication provider defines an authentication scheme that could be used to authenticate endpoints. The `auth:AuthStoreProvider` acts as the interface for all the authentication providers. Any type of implementation such as LDAP, JDBC, and file-based should be object-equivalent.

When creating a new authentication provider, you need to implement the below function.
- `authenticate` : Authenticates the user based on the user credentials (i.e., the username/password) or a token such as JWT.

By default, the `auth:ConfigAuthStoreProvider` authenticates based on usernames and passwords stored in a configuration file. The `auth:ConfigAuthStoreProvider` is an implementation of the `auth:AuthProvider` interface, which uses the Ballerina configuration file to store usernames, passwords, scopes, and relevant associations.

A user is denoted by a section in the configuration file. The password and the scopes assigned to the user are denoted as keys under the relevant user section as shown below.

 ```
 [b7a.users.<username>]
 password="<password>"
 scopes="<comma_separated_scopes>"
 ```
