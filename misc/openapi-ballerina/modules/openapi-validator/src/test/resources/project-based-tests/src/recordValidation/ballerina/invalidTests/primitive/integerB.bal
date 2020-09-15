import ballerina/http;

service hello on new http:Listener(9090) {
    @http:ResourceConfig {
            methods:["POST"],
            path:"/user/{userId}"
     }
    resource function sayHello(http:Caller caller, http:Request req, int userId ) returns error? {
    }
}
