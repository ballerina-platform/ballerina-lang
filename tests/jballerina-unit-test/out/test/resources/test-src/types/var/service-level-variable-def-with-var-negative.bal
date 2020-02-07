import ballerina/http;

service DummyService on new http:MockListener(9090) {
    var dummy = "dummy-string";

    resource function dummyResource (http:Caller caller, http:Request req) {
        json responseJson = {"dummy":"dummy"};
        http:Response res = new;
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}
