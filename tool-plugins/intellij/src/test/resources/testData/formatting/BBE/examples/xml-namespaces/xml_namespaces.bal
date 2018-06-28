import ballerina/io;

xmlns "http://ballerina.com/aa" as ns0;

function main(string... args) {
    // Namespaces can be declared at package level as well as at function level. The identifier followed by the `as` keyword is the prefix bound to this namespace name.
    xmlns "http://ballerina.com/bb" as ns1;

    // Namespace declaration without the prefix. This will define a default namespace.
    xmlns "http://ballerina.com/default";

    // Namespaces can be used for XML qualified names.
    io:println(ns0:foo);

    // Package level namespaces can be overridden at function level.
    xmlns "http://ballerina.com/updated" as ns0;
    io:println(ns0:foo);
}
