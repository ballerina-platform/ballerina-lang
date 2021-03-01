# Example for an xml literal:
# ```xml x = xml `<{{tagName}}>hello</{{tagName}}>`;```
final string testConst = "TestConstantDocumentation";

# ```this is to test `(single backtick) and ``(double backtick) within triple backticks```
final int testConst = 21;

# This is a dummy object
# ```
# Purpose of adding
# this documentation is
# to check backtick documentations
# ```
type DummyObject object {

    # This is a test function
    # ```
    # Purpose of adding
    # this documentation is
    # to check backtick documentations
    # ```
    public function func1();
};

# Prints value(s) to the STDOUT.
# ```ballerina
# io:print("Start processing the CSV file from ", srcFileName);
#
# io:print("Start processing the CSV file from ", srcFileName);
# ```
public function print() {
}

# Represents the LDAP based listener Basic Auth provider. This connects to an active directory or an LDAP,
# retrieves the necessary user information, and performs authentication and authorization. This is an implementation
# of the `auth:ListenerBasicAuthProvider` object.
# ```ballerina
# auth:LdapUserStoreConfig config = {
#      domainName: "ballerina.io",
#      connectionURL: "ldap://localhost:389",
#      connectionName: "cn=admin,dc=avix,dc=lk"
# };
#  auth:ListenerLdapUserStoreBasicAuthProvider provider = new(config);
# ```
public class ListenerLdapUserStoreBasicAuthProvider {
}
