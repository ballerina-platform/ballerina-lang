import ballerina/http;

type Category record {
    string id;
    string name;
};
service hello on new http:Listener(9090) {
    @http:ResourceConfig {
            methods:["POST"],
            path:"/user/{category}"
    }
    resource function sayHello(http:Caller caller, http:Request req, Category [] category ) returns error? {
    }
}
