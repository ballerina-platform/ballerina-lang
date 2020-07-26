import ballerina/http;

@http:ServiceConfig {
    basePath : "/addService"
}
service addService on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/add"
    }
    resource function add(http:Caller caller, http:Request req) returns error? {
        http:Response response  = new;
        int val = intAdd(10, 6);
        json payload = {
            value : val
        };
        response.setJsonPayload(payload);
        check caller->respond(response);
    }
}

// Function to be mocked
public function intAdd(int a, int b) returns (int) {
    return a + b;
}

// Function calling the original
public function callIntAdd(int a, int b) returns (int) {
    return intAdd(a, b);
}

// Function to be mocked from another module
public function intAdd2(int a, int b) returns (int) {
    return a + b;
}