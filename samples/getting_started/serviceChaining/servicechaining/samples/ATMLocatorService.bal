package servicechaining.samples;

import ballerina.net.http;
import ballerina.lang.system;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:Request req, http:Response resp) {
        http:ClientConnector bankInfoService = create http:ClientConnector("http://localhost:9090/bankinfo/product", {});
        http:ClientConnector branchLocatorService = create http:ClientConnector("http://localhost:9090/branchlocator/product", {});
        
        http:Request backendServiceReq = {};
        json jsonLocatorReq = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string) jsonLocatorReq["ATMLocator"]["ZipCode"];
        system:println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator": {"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        backendServiceReq.setJsonPayload(branchLocatorReq);

        http:Response locatorResponse = {};
        locatorResponse = branchLocatorService.post("", backendServiceReq);
        json branchLocatorRes = locatorResponse.getJsonPayload();
        string branchCode;
        branchCode, _ = (string) branchLocatorRes.ABCBank.BranchCode;
        system:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo": {"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        backendServiceReq.setJsonPayload(bankInfoReq);

        http:Response infoResponse = {};
        infoResponse = bankInfoService.post("", backendServiceReq);
        resp.forward(infoResponse);
    
    }    
}
