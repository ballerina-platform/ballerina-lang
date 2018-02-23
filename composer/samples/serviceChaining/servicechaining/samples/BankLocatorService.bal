package servicechaining.samples;

import ballerina.net.http;

@http:configuration {basePath:"/branchlocator"}
service<http> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Connection conn, http:InRequest req) {
        json jsonRequest = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank":{"BranchCode":"123"}};
        } else {
            payload = {"ABCBank":{"BranchCode":"-1"}};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
