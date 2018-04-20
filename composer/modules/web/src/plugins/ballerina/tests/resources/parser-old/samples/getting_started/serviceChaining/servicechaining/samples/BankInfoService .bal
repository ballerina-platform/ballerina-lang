
import ballerina/lang.messages;
import ballerina/http;

@http:configuration {basePath:"/bankinfo"}
service<http> Bankinfo {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (message m) {
        message response = {};
        json jsonRequest = messages:getJsonPayload(m);
        string branchCode;
        branchCode, _ = (string) jsonRequest.BranchInfo.BranchCode;
        json payload = {};
        if (branchCode == "123") {
            payload = {"ABC Bank": {"Address": "111 River Oaks Pkwy, San Jose, CA 95999"}};
            
        }
        else {
            payload = {"ABC Bank": {"error": "No branches found."}};
            
        }
        messages:setJsonPayload(response, payload);
        response:send(response);   
    }
}
