import ballerina/io;

documentation {
   Prints `Hello World`.
}
function testPackageFunction(string param1) {
    int localVar1 = 12;
    string localVar2 = "Hello"; 
    io:println("Hello World!");
}
