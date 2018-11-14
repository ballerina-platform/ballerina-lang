import ballerina/io;
import ballerina/mime;
import ballerina/http;

endpoint http:Listener serviceEnpoint {
    port:9092
};

endpoint http:Client bankInfoService {
    url: "http://localhost:9092/bankinfo/product"
};

endpoint http:Client branchLocatorService {
    url: "http://localhost:9092/branchlocator/product"
};

@http:ServiceConfig {
    basePath:"/ABCBank"
}
service<http:Service> ATMLocator bind serviceEnpoint {
    @http:ResourceConfig {
        methods:["POST"]
    }
    locator (endpoint caller, http:Request req) {

        http:Request backendServiceReq = new;
        var jsonLocatorReq = req.getJsonPayload();
        if (jsonLocatorReq is json) {
            string zipCode = extractFieldValue2(jsonLocatorReq["ATMLocator"]["ZipCode"]);
            io:println("Zip Code " + zipCode);
            json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
            branchLocatorReq.BranchLocator.ZipCode = zipCode;
            backendServiceReq.setPayload(untaint branchLocatorReq);
        } else if (jsonLocatorReq is error) {
            io:println("Error occurred while reading ATM locator request");
        }

        http:Response locatorResponse = new;
        var locatorRes = branchLocatorService -> post("", backendServiceReq);
        if (locatorRes is http:Response) {
            locatorResponse = locatorRes;
        } else if (locatorRes is error) {
            io:println("Error occurred while reading locator response");
        }

        var branchLocatorRes = locatorResponse.getJsonPayload();
        if (branchLocatorRes is json) {
            string branchCode = extractFieldValue2(branchLocatorRes.ABCBank.BranchCode);
            io:println("Branch Code " + branchCode);
            json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
            bankInfoReq.BranchInfo.BranchCode = branchCode;
            backendServiceReq.setJsonPayload(untaint bankInfoReq);
        } else if (branchLocatorRes is error) {
            io:println("Error occurred while reading branch locator response");
        }

        http:Response infomationResponse = new;
        var infoRes = bankInfoService -> post("", backendServiceReq);
        if (infoRes is http:Response) {
            infomationResponse = infoRes;
        } else if (infoRes is error) {
            io:println("Error occurred while writing info response");
        }
        _ = caller -> respond(infomationResponse);
    }
}

@http:ServiceConfig {
    basePath:"/bankinfo"
}
service<http:Service> Bankinfo bind serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"]
    }
    product (endpoint caller, http:Request req) {
        http:Response res = new;
        var jsonRequest = req.getJsonPayload();
        if (jsonRequest is json) {
            string branchCode = extractFieldValue2(jsonRequest.BranchInfo.BranchCode);
            json payload = {};
            if (branchCode == "123") {
                payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
            } else {
                payload = {"ABC Bank":{"error":"No branches found."}};
            }
            res.setPayload(payload);
        } else if (jsonRequest is error) {
            io:println("Error occurred while reading bank info request");
        }

        _ = caller -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/branchlocator"
}
service<http:Service> Banklocator bind serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"]
    }
    product (endpoint caller, http:Request req) {
        http:Response res = new;
        var jsonRequest = req.getJsonPayload();
        if (jsonRequest is json) {
            string zipCode = extractFieldValue2(jsonRequest.BranchLocator.ZipCode);
            json payload = {};
            if (zipCode == "95999") {
                payload = {"ABCBank":{"BranchCode":"123"}};
            } else {
                payload = {"ABCBank":{"BranchCode":"-1"}};
            }
            res.setPayload(payload);
        } else if (jsonRequest is error) {
            io:println("Error occurred while reading bank locator request");
        }

        _ = caller -> respond(res);
    }
}

//Keep this until there's a simpler way to get a string value out of a json
function extractFieldValue2(json fieldValue) returns string {
    match fieldValue {
        int i => return "error";
        string s => return s;
        boolean b => return "error";
        ()  => return "error";
        json j => return "error";
    }
}
