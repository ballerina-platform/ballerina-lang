package servicechaining.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/branchlocator"}
service Banklocator {
    // Responds back to the caller with a predefined json payload based on the branch code received.
    
    @http:POST{}
    resource product (message m) {
        message response = {};
        json jsonRequest = messages:getJsonPayload(m);
        //an alternative way of accessing json objects.
        string zipCode = <string> jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank": {"BranchCode":"123"}};
            
        }
        else {
            payload = {"ABCBank": {"BranchCode":"-1"}};
            
        }
        messages:setJsonPayload(response, payload);
        reply response;
    }
}
