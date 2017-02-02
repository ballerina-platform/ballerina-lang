package foo.bar.xyz;

import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.lang.json;

@Description("Remove HTTP header from the message")
@Param("m: Incoming message")
@Param("key: HTTP header key")
public function removeHeader (message m, string key) {
    system:println("invoked");
}