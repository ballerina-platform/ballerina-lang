import ballerina.io;
import ballerina.net.http;

@http:configuration {basePath:"/ABCBank"}
service<http> ATMLocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> bankInfoService {
            create http:HttpClient("http://localhost:9090/bankinfo/product", {});
        }
        endpoint<http:HttpClient> branchLocatorService {
            create http:HttpClient("http://localhost:9090/branchlocator/product", {});
        }

        http:OutRequest backendServiceReq = {};
        http:HttpConnectorError err;
        json jsonLocatorReq = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonLocatorReq["ATMLocator"]["ZipCode"];
        io:println("Zip Code " + zipCode);
        json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
        branchLocatorReq.BranchLocator.ZipCode = zipCode;
        backendServiceReq.setJsonPayload(branchLocatorReq);

        http:InResponse locatorResponse = {};
        locatorResponse, err = branchLocatorService.post("", backendServiceReq);
        json branchLocatorRes = locatorResponse.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)branchLocatorRes.ABCBank.BranchCode;
        io:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        backendServiceReq.setJsonPayload(bankInfoReq);

        http:InResponse infoResponse = {};
        infoResponse, err = bankInfoService.post("", backendServiceReq);
        _ = conn.forward(infoResponse);
    }
}

@http:configuration {basePath:"/bankinfo"}
service<http> Bankinfo {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Connection conn, http:InRequest req) {
        json jsonRequest = req.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)jsonRequest.BranchInfo.BranchCode;
        json payload = {};
        if (branchCode == "123") {
            payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
        } else {
            payload = {"ABC Bank":{"error":"No branches found."}};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/branchlocator"}
service<http> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Connection conn, http:InRequest req) {
        json jsonRequest = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank":{"BranchCode":"123"}};
        } else {
            payload = {"ABCBank":{"BranchCode":"-1"}};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}