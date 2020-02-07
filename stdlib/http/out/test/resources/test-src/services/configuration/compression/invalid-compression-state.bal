import ballerina/http;

listener http:MockListener mockEP  = new(9093);

@http:ServiceConfig {compression: {
    enable: "AAUUTTOO",
    contentTypes:["text/plain"]
    }
}
service alwaysCompressWithBogusContentType on mockEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function test1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello World!!!");
        checkpanic caller->respond(res);
    }
}
