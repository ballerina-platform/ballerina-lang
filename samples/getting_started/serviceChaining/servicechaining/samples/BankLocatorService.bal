package servicechaining.samples;

import ballerina.net.http;

@http:configuration {basePath:"/branchlocator"}
service<http> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Request req, http:Response res) {
        json jsonRequest = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank":{"BranchCode":"123"}};
        } else {
            payload = {"ABCBank":{"BranchCode":"-1"}};
        }
        res.setJsonPayload(payload);
        res.send();
    }
}
