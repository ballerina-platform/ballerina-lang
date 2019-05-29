import ballerina/http;

http:Client clientEP = new("http://localhost:9092/hello");

listener http:MockListener mockEP = new(9090, config= {server:"Mysql"});

service passthrough on mockEP {

    @http:ResourceConfig {
        path: "/"
    }

    resource function passthrough(http:Caller caller, http:Request req) {
        var clientResponse = clientEP->forward("/", req);
        if (clientResponse is http:Response) {
            checkpanic caller->respond(clientResponse);
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(<string>clientResponse.detail().message);
            checkpanic caller->respond(res);
        }
    }
}

service hello on new http:Listener(9092, config= {server:"Mysql"}) {

    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function helloResource(http:Caller caller, http:Request req) {

        checkpanic caller->respond("Hello World!");
    }
}
