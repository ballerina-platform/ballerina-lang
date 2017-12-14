package servicechaining.samples;

import ballerina.net.http;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:Request req, http:Response resp) {
        endpoint<http:HttpClient> bankInfoService {
            create http:HttpClient("http://localhost:9090/bankinfo/product", {});
        }
        endpoint<http:HttpClient> branchLocatorService {
            create http:HttpClient("http://localhost:9090/branchlocator/product", {});
        }

        http:Request backendServiceReq = {};
        http:HttpConnectorError err;
        json jsonLocatorReq = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonLocatorReq["ATMLocator"]["ZipCode"];
        println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        backendServiceReq.setJsonPayload(branchLocatorReq);

        http:Response locatorResponse = {};
        locatorResponse, err = branchLocatorService.post("", backendServiceReq);
        json branchLocatorRes = locatorResponse.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)branchLocatorRes.ABCBank.BranchCode;
        println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        backendServiceReq.setJsonPayload(bankInfoReq);

        http:Response infoResponse = {};
        infoResponse, err = bankInfoService.post("", backendServiceReq);
        _ = resp.forward(infoResponse);
    }
}
