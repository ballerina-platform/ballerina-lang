import ballerina/lang.test;

listener test:MockListener pathEP = new(9090);

const string RESOURCE_PATH = "/{id}";
const string SERVICE_BASE_PATH = "/hello";
const string SERVICE_HOST = "b7a.default";
const string RESOURCE_BODY = "person";

type ser service object {
};

@test:ServiceConfig {
    basePath: SERVICE_BASE_PATH,
    host: SERVICE_HOST
}
service ser on pathEP {

    @test:ResourceConfig {
        path: RESOURCE_PATH,
        methods: ["GET", "POST"],
        body: RESOURCE_BODY
    }

     resource function get hello(string req, string person) {
        json responseJson = { "Person": person };
        test:Caller caller = new test:Caller();
        string res = caller->respond(req);
    }
}
