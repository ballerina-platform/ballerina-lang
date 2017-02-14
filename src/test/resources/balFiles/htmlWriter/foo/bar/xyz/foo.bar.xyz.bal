package foo.bar.xyz;

import ballerina.lang.system;

struct Argument {
    string text;
    int argumentId;
    int sentiment;
}

@doc:Description("Remove HTTP header from the message")
@doc:Param("m: Incoming message")
@doc:Param("key: HTTP header key")
function removeHeader (message m, string key) {
    system:println("invoked");
    Argument arg1 = {text:"arg1", argumentId:1, sentiment:1};
    testStruct(arg1);
}

@doc:Description("Test struct data type")
@doc:Param("argument: Incoming argument")
function testStruct(Argument argument) {
    system:println("Hello, World!");
}


