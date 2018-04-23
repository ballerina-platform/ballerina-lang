
import ballerina/http;
import ballerina/http.request;
import ballerina/http.response;

@http:configuration {basePath:"/branchlocator"}
service<http> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Request req, http:Response res) {
        json jsonRequest = request:getJsonPayload(req);
        string zipCode;
        zipCode, _ = (string) jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank": {"BranchCode":"123"}};
            
        }
        else {
            payload = {"ABCBank": {"BranchCode":"-1"}};
            
        }
        response:setJsonPayload(res, payload);
        response:send(res);
    }
}
