import ballerina.lang.system;

//demonstrates how multiple return values work.

function main (string[] args) {
    system:println(multiValueReturn("hello"));
}

function multiValueReturn(string s) (string, int, string){
    return split(s), 5, "ss";
}

function split(string s) (string, int) {
    return s, 4;
}