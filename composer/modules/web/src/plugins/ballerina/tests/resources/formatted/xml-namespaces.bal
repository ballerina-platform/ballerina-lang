import ballerina/lang.system;

xmlns "http://ballerina.com/aa" as ns0;

function main (string... args) {

    // Namespaces can be declared at package levels, as well as function level. The identifier followed by 'as' keyword is the prefix bound to this namespace name.
    xmlns "http://ballerina.com/bb" as ns1;

    // Namespace declaration without the prefix, will define a default namespace.
    xmlns "http://ballerina.com/default";

    // Namespaces can be used for xml qualified names.
    system:println(ns0:foo);

    // Package level namespaces can be overidden at function level.
    xmlns "http://ballerina.com/updated" as ns0;
    system:println(ns0:foo);
}
