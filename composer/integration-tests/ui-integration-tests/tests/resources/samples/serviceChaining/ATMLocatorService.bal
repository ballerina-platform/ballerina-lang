import ballerina/lang.messages;
import ballerina/http;
import ballerina/lang.system;
import ballerina/lang.jsons;
@http:BasePath ("/ABCBank")
service ATMLocator {
    
    @http:POST
    @http:Path ("/locator")
    resource locator (message m) {
        http:ClientConnector bankInfoService = create http:ClientConnector("http://localhost:9090/bankinfo");
        http:ClientConnector branchLocatorService = create http:ClientConnector("http://localhost:9090/branchlocator");
        message backendServiceReq = {};
        json jsonLocatorReq = messages:getJsonPayload(m);
        string zipCode = jsons:getString(jsonLocatorReq, "$.ATMLocator.ZipCode");
        system:println("Zip Code " + zipCode);
        json branchLocatorReq = `{"BranchLocator": {"ZipCode":""}}`;
        jsons:set(branchLocatorReq, "$.BranchLocator.ZipCode", zipCode);
        messages:setJsonPayload(backendServiceReq, branchLocatorReq);
        message response = http:ClientConnector.post(branchLocatorService, "", backendServiceReq);
        json branchLocatorRes = messages:getJsonPayload(response);
        string branchCode = jsons:getString(branchLocatorRes, "$.ABCBank.BranchCode");
        system:println("Branch Code " + branchCode);
        json bankInfoReq = `{"BranchInfo": {"BranchCode":""}}`;
        jsons:set(bankInfoReq, "$.BranchInfo.BranchCode", branchCode);
        messages:setJsonPayload(backendServiceReq, bankInfoReq);
        response = http:ClientConnector.post(bankInfoService, "", backendServiceReq);
        reply response;
    
    }
    
}
@http:BasePath ("/branchlocator")
service Banklocator {
    
    @http:POST
    resource product (message m) {
        message response = {};
        json jsonRequest = messages:getJsonPayload(m);
        string zipCode = jsons:getString(jsonRequest, "$.BranchLocator.ZipCode");
        json payload = {};
        if (zipCode == "95999") {
            payload = `{"ABCBank": {"BranchCode":"123"}}`;
            
        }
        else {
            payload = `{"ABCBank": {"BranchCode":"-1"}}`;
            
        }
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
@http:BasePath ("/bankinfo")
service Bankinfo {
    
    @http:POST
    resource product (message m) {
        message response = {};
        json jsonRequest = messages:getJsonPayload(m);
        string branchCode = jsons:getString(jsonRequest, "$.BranchInfo.BranchCode");
        json payload = {};
        if (branchCode == "123") {
            payload = `{"ABC Bank": {"Address": "111 River Oaks Pkwy, San Jose, CA 95999"}}`;
            
        }
        else {
            payload = `{"ABC Bank": {"error": "No branches found."}}`;
            
        }
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
