import ballerina/http;

listener http:Listener ep1 = new(9097, config = { httpVersion: "2.0" });

listener http:Listener ep2 = new(9098, config = { httpVersion: "2.0" });

http:Client h2WithPriorKnowledge = new("http://localhost:9098", config = { httpVersion: "2.0", http2Settings: {
        http2PriorKnowledge: true
    } });

http:Client h2WithoutPriorKnowledge = new("http://localhost:9098", config = { httpVersion: "2.0", http2Settings: {
        http2PriorKnowledge: false
    } });

@http:ServiceConfig {
    basePath: "/priorKnowledge"
}
service priorKnowledgeTest on ep1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/on"
    }
    resource function priorOn(http:Caller caller, http:Request req) {
        var response = h2WithPriorKnowledge->post("/backend", "Prior knowledge is enabled");
        if (response is http:Response) {
            checkpanic caller->respond(untaint response);
        } else {
            checkpanic caller->respond("Error in client post");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/off"
    }
    resource function priorOff(http:Caller caller, http:Request req) {
        var response = h2WithoutPriorKnowledge->post("/backend", "Prior knowledge is disabled");
        if (response is http:Response) {
            checkpanic caller->respond(untaint response);
        } else {
            checkpanic caller->respond("Error in client post");
        }
    }
}

@http:ServiceConfig {
    basePath: "/backend"
}
service testBackEnd on ep2 {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/"
    }
    resource function test(http:Caller caller, http:Request req) {
        string outboundResponse = "";
        if (req.hasHeader(http:CONNECTION) && req.hasHeader(http:UPGRADE)) {
            string[] connHeaders = req.getHeaders(http:CONNECTION);
            outboundResponse = connHeaders[1];
            outboundResponse = outboundResponse + "--" + req.getHeader(http:UPGRADE);
        } else {
            outboundResponse = "Connection and upgrade headers are not present";
        }

        outboundResponse = outboundResponse + "--" + checkpanic req.getTextPayload();
        checkpanic caller->respond(untaint outboundResponse);
    }
}
