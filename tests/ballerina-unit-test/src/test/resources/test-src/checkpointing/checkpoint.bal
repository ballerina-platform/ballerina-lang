import ballerina/io;
import ballerina/runtime;
import ballerina/http;

type Person object {
string name;
int age;
};

@http:ServiceConfig {
    basePath:"/s1"
}
service<http:Service> s1 bind { port: 9090 } {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/r1"
    }

    @Interruptible
    r1 (endpoint conn, http:Request req) {
        io:println( "Starting flow..");
        f1();
        http:Response res = new;
        res.setTextPayload("Test response");
        var response = conn -> respond(res);
        io:println( "Request completed");
    }
}

function f1() {
    Person p1 = new;
    p1.name = "smith";
    p1.age = 20;
    runtime:checkpoint();
    io:println( "Waiting..");
    runtime:sleep(5000);
    io:println( p1);
}
