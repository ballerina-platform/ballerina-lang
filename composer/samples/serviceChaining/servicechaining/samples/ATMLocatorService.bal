package servicechaining.samples;

import ballerina/io;
import ballerina/http;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> bankInfoService {
            create http:HttpClient("http://localhost:9090/bankinfo/product", {});
        }
        endpoint<http:HttpClient> branchLocatorService {
            create http:HttpClient("http://localhost:9090/branchlocator/product", {});
        }

        http:Request backendServiceReq = {};
        http:HttpConnectorError err;
        var jsonLocatorReq, _ = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonLocatorReq["ATMLocator"]["ZipCode"];
        io:println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        backendServiceReq.setJsonPayload(branchLocatorReq);

        http:Response locatorResponse = {};
        locatorResponse, err = branchLocatorService.post("", backendServiceReq);
        var branchLocatorRes, _ = locatorResponse.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)branchLocatorRes.ABCBank.BranchCode;
        io:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        backendServiceReq.setJsonPayload(bankInfoReq);

        http:Response infoResponse = {};
        infoResponse, err = bankInfoService.post("", backendServiceReq);
        _ = conn.forward(infoResponse);
    }
}
