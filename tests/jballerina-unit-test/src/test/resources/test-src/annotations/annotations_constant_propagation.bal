import ballerina/http;

listener http:Listener pathEP = new(9090);

const string RESOURCE_PATH = "/{id}";
const string SERVICE_BASE_PATH = "/hello";
const string SERVICE_HOST = "b7a.default";
const string RESOURCE_BODY = "person";

@http:ServiceConfig {
    basePath: SERVICE_BASE_PATH,
    host: SERVICE_HOST
}
service resourcePath on pathEP {

    @http:ResourceConfig {
        path: RESOURCE_PATH,
        methods: ["GET", "POST"],
        body: RESOURCE_BODY
    }

    resource function hello(http:Caller caller, http:Request req, string id, string person) {
        json responseJson = { "Person": person };
        checkpanic caller->respond(<@untainted json> responseJson);
    }
}
