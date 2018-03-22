import ballerina/io;
import ballerina/net.http;

endpoint<http:Service> serviceEnpoint {
    port:9090
}

endpoint<http:Client> bankInfoService {
    serviceUri: "http://localhost:9090/bankinfo/product"
}
endpoint<http:Client> branchLocatorService {
    serviceUri: "http://localhost:9090/branchlocator/product"
}

@http:serviceConfig {
    basePath:"/ABCBank",
    endpoints:[serviceEnpoint]
}
service<http:Service> ATMLocator {
    @http:resourceConfig {
        methods:["POST"]
    }
    resource locator (http:ServerConnector conn, http:Request req) {
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
        locatorResponse, err = branchLocatorService -> post("", backendServiceReq);
        var branchLocatorRes, _ = locatorResponse.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)branchLocatorRes.ABCBank.BranchCode;
        io:println("Branch Code " + branchCode);
        json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
        bankInfoReq.BranchInfo.BranchCode = branchCode;
        backendServiceReq.setJsonPayload(bankInfoReq);

        http:Response infoResponse = {};
        infoResponse, err = bankInfoService -> post("", backendServiceReq);
        _ = conn -> forward(infoResponse);
    }
}

@http:serviceConfig {
    basePath:"/bankinfo",
    endpoints:[serviceEnpoint]
}
service<http:Service> Bankinfo {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:ServerConnector conn, http:Request req) {
        var jsonRequest, _ = req.getJsonPayload();
        string branchCode;
        branchCode, _ = (string)jsonRequest.BranchInfo.BranchCode;
        json payload = {};
        if (branchCode == "123") {
            payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
        } else {
            payload = {"ABC Bank":{"error":"No branches found."}};
        }

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/branchlocator",
    endpoints:[serviceEnpoint]
}
service<http:Service> Banklocator {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:ServerConnector conn, http:Request req) {
        var jsonRequest, _ = req.getJsonPayload();
        string zipCode;
        zipCode, _ = (string)jsonRequest.BranchLocator.ZipCode;
        json payload = {};
        if (zipCode == "95999") {
            payload = {"ABCBank":{"BranchCode":"123"}};
        } else {
            payload = {"ABCBank":{"BranchCode":"-1"}};
        }

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}