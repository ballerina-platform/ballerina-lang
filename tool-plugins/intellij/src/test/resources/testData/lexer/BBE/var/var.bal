import ballerina/io;

function main(string... args) {
    //The variable type is inferred from the initial value. This is the same as `int k = 5`;
    var k = 5;
    io:println(10 + k);

    //The type of `strVar` is `string`.
    var strVar = "Hello!";
    io:println(strVar);
}
