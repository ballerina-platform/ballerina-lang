import ballerina/lang.'int as 'error;
import ballerina/lang.'decimal as 'xml;
import ballerina/lang.'string as 'object;

function testMax(int n, int m) returns int {
    return 'error:max(n, m);
}

function testMin(int n, int m) returns int {
    return 'error:min(n, m);
}

function testSum(decimal p1, decimal p2) returns decimal {
    return 'xml:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return 'xml:max(arg);
}

function testStartsWith() returns boolean {
    return 'object:startsWith(" Chiran Sachintha ", "Hello");
}

function testStringConcat() returns string {
    return 'object:concat("Hello ", "from ", "Ballerina");
}
