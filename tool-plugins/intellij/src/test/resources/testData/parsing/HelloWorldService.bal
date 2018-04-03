import ballerina/http;
import ballerina/http.response;

service<http> helloWorld {

    resource sayHello (http:Request req, http:Response res) {
        response:setStringPayload(res, "Hello, World!");
        response:send(res);
    }
}
