import ballerina/io;
import ballerina/runtime;
import ballerina/http;

type Person object {
    string name;
    int age;
};

@http:ServiceConfig {
    basePath: "/s1"
}
service<http:Service> s1 bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/r1"
    }

    @Interruptible
    r1(endpoint conn, http:Request req) {
        io:println("Starting flow..");
        http:Response res = new;
        res.setTextPayload("State created");
        var response = conn->respond(res);
        f1();
        io:println("State completed");
    }
}

function f1() {
    Person p1 = new;
    p1.name = "smith";
    p1.age = 20;
    runtime:checkpoint();
    io:println("Waiting..");
    runtime:sleep(6000);
    io:println(p1);
}
