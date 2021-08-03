// empty main bal
import ballerina/test;
import waruna/http.'client as httpClient;
import waruna/websub.server as websubServer;
import myPackage.mod1;
import ballerina/jballerina.java;

public function main() {
    test:assertEquals(getHtppVersion(), "1.0.1");
    test:assertEquals(getWebSubHtppVersion(), "1.0.1");
    test:assertEquals(mod1:getHtppVersion(), "1.0.1");
    test:assertEquals(mod1:getWebSubHtppVersion(), "1.0.1");
    print("Tests passed");
}


public function getWebSubHtppVersion() returns string {
     return websubServer:getHttpVersion();
}

public function getHtppVersion() returns string {
     return httpClient:getVersion();
}

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
