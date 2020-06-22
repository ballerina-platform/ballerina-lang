import ballerina/io;

xmlns "http://ballerina.com/aa" as ns0;

public function main() {
    // Namespaces can be declared at the module level as well as at the function level. The identifier followed by the
    // `as` keyword is the prefix bound to this namespace name.
    xmlns "http://ballerina.com/bb" as
    ns1
    ;
    xmlns "http://ballerina.com/default";

    // Namespaces can be used for XML-qualified names.
    io:println(ns0:foo);

    // Module level namespaces can be overridden at the function level.
    xmlns
        "http://ballerina.com/updated" as ns0;
    io:println(ns0:foo);
}
