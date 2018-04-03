package servicechaining.samples;

import ballerina/http;

@http:configuration {basePath:"/branchlocator"}
service<http> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Connection conn, http:Request req) {
        var jsonRequest, payloadError = req.getJsonPayload();
        http:Response res = {};
        if (payloadError == null) {
            string zipCode;
            zipCode, _ = (string)jsonRequest.BranchLocator.ZipCode;
            json payload = {};
            if (zipCode == "95999") {
                payload = {"ABCBank":{"BranchCode":"123"}};
            } else {
                payload = {"ABCBank":{"BranchCode":"-1"}};
            }
            res.setJsonPayload(payload);
        } else {
            res.statusCode = 500;
            res.setStringPayload(payloadError.message);
        }
        _ = conn.respond(res);
    }
}
