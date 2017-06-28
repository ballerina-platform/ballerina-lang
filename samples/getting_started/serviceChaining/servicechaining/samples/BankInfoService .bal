package servicechaining.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/bankinfo"}
service Bankinfo {
    //Responds back to the caller with a predefined json payload based on the branch code received.
    
    @http:POST{}
    resource product (message m) {
        message response = {};
        json jsonRequest = messages:getJsonPayload(m);
        //an alternative way of accessing json objects.
        string branchCode = <string> jsonRequest.BranchInfo.BranchCode;
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
