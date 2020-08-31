import ballerina/http;
import ballerina/openapi;

type Pet record {
     int id;
     string name;
     string tag;
     string 'type;
};
type Dog record {
    *Pet;
     boolean bark;
};
type Cat record {
     int id;
     string name;
     string tag;
     string 'type;
};
type Error record {
     int code;
     string message;
};
listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});
listener http:Listener ep1 = new(9090);
@openapi:ServiceInfo {
    contract: "resources/oneOf-with-parameter.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}
service parameter_service on ep0, ep1 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/oneOfRequestBody",
        body:"body"
    }
    resource function getPets (http:Caller caller, http:Request req,  int limit,  Dog| Cat| any  body) returns error? {
    }

}
