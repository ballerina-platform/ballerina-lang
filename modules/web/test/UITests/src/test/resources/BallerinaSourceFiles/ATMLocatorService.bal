import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.json;

@BasePath ("/ABCBank")
service ATMLocator {

    @POST
    @Path ("/locator")
    resource locator (message m) {
        http:HTTPConnector branchLocatorService = new http:HTTPConnector("http://localhost:9090/branchlocator");
        http:HTTPConnector bankInfoService = new http:HTTPConnector("http://localhost:9090/bankinfo");
        message response;
        message backendServiceReq;

        json branchLocatorReq;
        json bankInfoReq;

        string zipCode;
        string branchCode;

        json jsonLocatorReq;
        json branchLocatorRes;

        jsonLocatorReq = message:getJsonPayload(m);
        zipCode = json:getString(jsonLocatorReq, "$.ATMLocator.ZipCode");
        system:println("Zip Code " + zipCode);

        branchLocatorReq = `{"BranchLocator": {"ZipCode":""}}`;
        json:set(branchLocatorReq, "$.BranchLocator.ZipCode", zipCode);
        message:setJsonPayload(backendServiceReq, branchLocatorReq);
        response = http:HTTPConnector.post(branchLocatorService, "", backendServiceReq);

        branchLocatorRes = message:getJsonPayload(response);

        branchCode = json:getString(branchLocatorRes, "$.ABCBank.BranchCode");
        system:println("Branch Code " + branchCode);

        bankInfoReq = `{"BranchInfo": {"BranchCode":""}}`;

        json:set(bankInfoReq, "$.BranchInfo.BranchCode", branchCode);
        message:setJsonPayload(backendServiceReq, bankInfoReq);

        response = http:HTTPConnector.post(bankInfoService, "", backendServiceReq);

        reply response;
    }
}


@BasePath("/branchlocator")
service Banklocator {

    @POST
    resource product (message m) {
        message response;
        json payload;
        json jsonRequest;
        string zipCode;

        jsonRequest = message:getJsonPayload(m);
        zipCode = json:getString(jsonRequest, "$.BranchLocator.ZipCode");

        if (zipCode == "95999") {
            payload = `{"ABCBank": {"BranchCode":"123"}}`;
        } else {
            payload = `{"ABCBank": {"BranchCode":"-1"}}`;
        }
        message:setJsonPayload(response, payload);
        reply response;
    }
}


@BasePath("/bankinfo")

service Bankinfo {

    @POST
    resource product (message m) {
        message response;
        json payload;
        json jsonRequest;
        string branchCode;


        jsonRequest = message:getJsonPayload(m);
        branchCode = json:getString(jsonRequest, "$.BranchInfo.BranchCode");
        if (branchCode == "123") {
            payload = `{"ABC Bank": {"Address": "111 River Oaks Pkwy, San Jose, CA 95999"}}`;
        } else {
            payload = `{"ABC Bank": {"error": "No branches found."}}`;

        }

        message:setJsonPayload(response, payload);
        reply response;
    }
}


