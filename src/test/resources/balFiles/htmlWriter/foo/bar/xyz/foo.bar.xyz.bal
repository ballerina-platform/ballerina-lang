package foo.bar.xyz;

import ballerina.lang.system;

@Description("Remove HTTP header from the message")
@Param("m: Incoming message")
@Param("key: HTTP header key")
function removeHeader (message m, string key) {
    system:println("invoked");
}

struct Argument {
    string text;
    int argumentId;
    int sentiment;
}
