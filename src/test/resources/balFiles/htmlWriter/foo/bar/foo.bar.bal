package foo.bar;

import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.lang.json;

@Description("Add HTTP header to the message")
@Param("m: Incoming message")
@Param("key: HTTP header key")
@Param("value: HTTP header value")
public function addHeader (message m, string key, string value) {
    system:println("invoked");
}

@Description("Get HTTP header from the message")
@Param("m: Incoming message")
@Param("key: HTTP header key")
@Return("value: HTTP header value")
public function getHeader (message m, string key) (string value) {
    system:println("invoked");
    return value;
}
