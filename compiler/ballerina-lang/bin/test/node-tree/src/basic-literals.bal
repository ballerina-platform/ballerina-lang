import ballerina/io;

public function main(string... args) {
    testBasicLiterals();
}

function testBasicLiterals(){
   // string literal
   io:println("Hello world!");
   // int literal
   io:println(1);
   // int hex
   io:println(0xff);
   // floating
   io:println(1.1); // parser not supported yet
   // floating hex
   io:println(55e-1); // parser not supported yet
   // boolean literal
   io:println(true);
   io:println(false);
}
