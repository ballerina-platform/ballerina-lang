package servicechaining.samples;

import ballerina/http;
import ballerina/http.request;
import ballerina/http.response;

@http:configuration {basePath:"/bankinfo"}
service<http> Bankinfo {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Request req, http:Response res) {
        json jsonRequest = request:getJsonPayload(req);
        string branchCode;
        branchCode, _ = (string) jsonRequest.BranchInfo.BranchCode;
        json payload = {};
        if (branchCode == "123") {
            payload = {"ABC Bank": {"Address": "111 River Oaks Pkwy, San Jose, CA 95999"}};
            
        }
        else {
            payload = {"ABC Bank": {"error": "No branches found."}};
            
        }
        response:setJsonPayload(res, payload);
        response:send(res);   
    }
}
