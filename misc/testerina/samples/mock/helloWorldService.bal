package mock;

import ballerina/http;
import ballerina/io;

endpoint http:Listener helloEP {
    port: 9092
};

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> HelloServiceMock bind helloEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    getEvents (endpoint client, http:Request req) {
        http:Response res = new;
        json j = {"Hello":"World"};
        res.setJsonPayload(j);
        _ = client -> respond(res);
    }
}