import ballerina/http;

listener http:Listener ep0 = new(9090, config = {host: "localhost"});

@http:ServiceConfig {
        basePath: "/v2"
}
service petstore_service on ep0 {

@http:ResourceConfig {
        methods:["POST"],
        path:"/user",
        body:"body"
        }
        resource function createUser (http:Caller caller, http:Request req,  User  body) returns error? {
        }

}
