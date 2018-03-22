package servicechaining.samples;

import ballerina/net.http;
import ballerina/lang.system;
import ballerina/net.http.request;
import ballerina/net.http.response;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:Request req, http:Response res) {
        http:ClientConnector bankInfoService = create http:ClientConnector("http://localhost:9090/bankinfo/product");
        http:ClientConnector branchLocatorService = create http:ClientConnector("http://localhost:9090/branchlocator/product");
        
        http:Request backendServiceReq = {};
        json jsonLocatorReq = request:getJsonPayload(req);
        string zipCode;
        zipCode, _ = (string) jsonLocatorReq["ATMLocator"]["ZipCode"];
        system:println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator": {"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        request:setJsonPayload(backendServiceReq, branchLocatorReq);
        
        res = branchLocatorService.post("", backendServiceReq);
        json branchLocatorRes = response:getJsonPayload(res);
        string branchCode;
        branchCode, _ = (string) branchLocatorRes.ABCBank.BranchCode;
        system:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo": {"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        request:setJsonPayload(backendServiceReq, bankInfoReq);
        res = bankInfoService.post("", backendServiceReq);
        
        response:send(res);
    
    }    
}
