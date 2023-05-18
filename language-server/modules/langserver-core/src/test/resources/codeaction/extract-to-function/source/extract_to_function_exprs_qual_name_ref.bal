import ballerina/module1 as mod;

function testFunction() {
    int localVar = mod:GLOBAL_VAR;
    int maxVal = int:max(1, 2);
}
