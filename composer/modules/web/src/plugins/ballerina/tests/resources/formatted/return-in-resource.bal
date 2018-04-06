import ballerina/http;

service<http> helloWorld {

    resource sayHello (http:Request req, http:Response res) {
        http:Response resp = {};
        return resp;
    }
}
