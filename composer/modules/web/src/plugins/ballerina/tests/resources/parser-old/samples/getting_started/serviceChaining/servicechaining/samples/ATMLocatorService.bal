package servicechaining.samples;

import ballerina/lang.messages;
import ballerina/http;
import ballerina/lang.system;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (message m) {
        http:ClientConnector bankInfoService = create http:ClientConnector("http://localhost:9090/bankinfo/product");
        http:ClientConnector branchLocatorService = create http:ClientConnector("http://localhost:9090/branchlocator/product");
        
        message backendServiceReq = {};
        json jsonLocatorReq = messages:getJsonPayload(m);
        string zipCode;
        zipCode, _ = (string) jsonLocatorReq["ATMLocator"]["ZipCode"];
        system:println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator": {"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        messages:setJsonPayload(backendServiceReq, branchLocatorReq);
        
        message response = branchLocatorService.post("", backendServiceReq);
        json branchLocatorRes = messages:getJsonPayload(response);
        string branchCode;
        branchCode, _ = (string) branchLocatorRes.ABCBank.BranchCode;
        system:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo": {"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        messages:setJsonPayload(backendServiceReq, bankInfoReq);
        response = bankInfoService.post("", backendServiceReq);
        
        response:send(response);
    
    }    
}
