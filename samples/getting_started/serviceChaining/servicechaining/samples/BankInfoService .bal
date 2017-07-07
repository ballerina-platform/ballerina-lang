package servicechaining.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:config {basePath:"/bankinfo"}
service<http> Bankinfo {
    
    @http:POST{}
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
        reply response;   
    }
}
