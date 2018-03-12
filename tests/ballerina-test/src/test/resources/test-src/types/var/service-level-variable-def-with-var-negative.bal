import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;


@http:configuration {basePath:"/dummy-path"}
service<http> DummyService {
    int counter = 100;
    var dummy = "dummy-string";

    @http:resourceConfig {
        methods:["GET"],
        path:"/dummy-context-path"
    }
    resource dummyResource (http:Request req, http:Response res) {
        json responseJson = {"dummy":"dummy"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

}

