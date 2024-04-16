import ballerina/jballerina.java;

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;


public class Listener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
        return ();
    }
}

listener Listener lsn = new;

service /hello on lsn {

    function init() {
        println(system_out(), java:fromString("Hello, World!"));
    }

    resource function get foo() returns int {
        return 1;
    }
}
