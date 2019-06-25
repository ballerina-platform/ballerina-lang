## Module Overview

This module provides inbound LDAP authentication provider, which can be used to authenticate using LDAP credentials.

The `ldap:InboundLdapAuthProvider` is another implementation of the `auth:InboundAuthProvider` interface. This connects to an active directory or an LDAP, retrieves the necessary user information, and performs authentication and authorization.

```ballerina
ldap:InboundLdapAuthProvider ldapAuthProvider = new(ldapConfig, "instanceId");
```
