import ballerina/iox;

# Prints `Hello World`.
function testPackageFunction(string param1) {
    int localVar1 = 12;
    string localVar2 = "Hello"; 
    iox:println("Hello World!");
}
