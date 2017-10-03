import ballerina.lang.messages;
import ballerina.net.http;



@http:BasePath {value:"/dummy-path"}
service DummyService {
    int counter = 100;
    var dummy = "dummy-string";

    @http:GET{}
    @http:Path {value:"/dummy-context-path"}
    resource dummyResource (message m) {
        message response = {};
        json responseJson = {"dummy":"dummy"};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

}

