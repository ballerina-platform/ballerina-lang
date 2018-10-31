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
        match jsonLocatorReq {
            json zip => {
                string zipCode;
                zipCode = extractFieldValue2(zip["ATMLocator"]["ZipCode"]);
                io:println("Zip Code " + zipCode);
                json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
                branchLocatorReq.BranchLocator.ZipCode = zipCode;
                backendServiceReq.setJsonPayload(untaint branchLocatorReq);
            }
            error err => {
                io:println("Error occurred while reading ATM locator request");
            }
        }

        http:Response locatorResponse = new;
        var locatorRes = branchLocatorService -> post("", backendServiceReq);
        match locatorRes {
            http:Response locRes => {
                locatorResponse = locRes;
            }
            error err => {
                io:println("Error occurred while reading locator response");
            }
        }

        var branchLocatorRes = locatorResponse.getJsonPayload();
        match branchLocatorRes {
            json branch => {
                string branchCode;
                branchCode = extractFieldValue2(branch.ABCBank.BranchCode);
                io:println("Branch Code " + branchCode);
                json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
                bankInfoReq.BranchInfo.BranchCode = branchCode;
                backendServiceReq.setJsonPayload(untaint bankInfoReq);
            }
            error err => {
                io:println("Error occurred while reading branch locator response");
            }
        }

        http:Response infomationResponse = new;
        var infoRes = bankInfoService -> post("", backendServiceReq);
        match infoRes {
            http:Response res => {
                infomationResponse = res;
            }
            error err => {
                io:println("Error occurred while writing info response");
            }
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
        match jsonRequest {
            json bankInfo => {
                string branchCode;
                branchCode = extractFieldValue2(bankInfo.BranchInfo.BranchCode);
                json payload = {};
                if (branchCode == "123") {
                    payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
                } else {
                    payload = {"ABC Bank":{"error":"No branches found."}};
                }

                res.setJsonPayload(payload);
            }
            error err => {
                io:println("Error occurred while reading bank info request");
            }
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
        match jsonRequest {
            json bankLocator => {
                string zipCode;
                zipCode = extractFieldValue2(bankLocator.BranchLocator.ZipCode);
                json payload = {};
                if (zipCode == "95999") {
                    payload = {"ABCBank":{"BranchCode":"123"}};
                } else {
                    payload = {"ABCBank":{"BranchCode":"-1"}};
                }
                res.setJsonPayload(payload);
            }
            error err => {
                io:println("Error occurred while reading bank locator request");
            }
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