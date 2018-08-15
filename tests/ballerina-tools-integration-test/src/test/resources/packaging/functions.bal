import ballerina/io;
function printValue(string value) {
    io:println(value);
}
function add(int a, int b) returns (int) {
    return a + b;
}

public function main(string... args) {
   printValue("This is a sample text");
   int result = add(5, 6);
   io:println(result);
}
