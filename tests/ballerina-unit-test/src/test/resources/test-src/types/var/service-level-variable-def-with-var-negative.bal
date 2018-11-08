import ballerina/http;

service<http:Service> DummyService bind { port:9090} {
    int counter = 100;
    var dummy = "dummy-string";

    dummyResource (endpoint caller, http:Request req) {
        json responseJson = {"dummy":"dummy"};
        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}
