import ballerina/http;

service<http:Service> Service1 {

    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello World!!!");
        _ = outboundEP -> respond(response);
    }
}
