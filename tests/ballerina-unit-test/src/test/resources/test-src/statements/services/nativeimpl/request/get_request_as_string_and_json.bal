import ballerina/io;
import ballerina/http;

endpoint http:NonListener testEP {
    port:9093
};

@http:ServiceConfig { 
    basePath: "/foo" 
}
service<http:Service> MyService bind testEP {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/bar"
    }
    myResource (endpoint caller, http:Request req) {
        string s = check req.getPayloadAsString();
    	json payload = check req.getJsonPayload();

        http:Response res;
        res.setPayload(untaint payload.foo);
        caller->respond(res) but { error e => io:println("Error sending response") };
    }
}
