package servicechaining.samples;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.system;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {
    
    @http:POST{}
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
        
        message response = http:ClientConnector.post(branchLocatorService, "", backendServiceReq);
        json branchLocatorRes = messages:getJsonPayload(response);
        string branchCode;
        branchCode, _ = (string) branchLocatorRes.ABCBank.BranchCode;
        system:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo": {"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        messages:setJsonPayload(backendServiceReq, bankInfoReq);
        response = http:ClientConnector.post(bankInfoService, "", backendServiceReq);
        
        reply response;
    
    }    
}
