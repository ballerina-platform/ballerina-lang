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
        http:HTTPConnector branchLocatorService = create http:HTTPConnector("http://localhost:9090/branchlocator");
        http:HTTPConnector bankInfoService = create http:HTTPConnector("http://localhost:9090/bankinfo");

        message backendServiceReq = {};

        string zipCode = json:getString(jsonLocatorReq, "$.ATMLocator.ZipCode");
        system:println("Zip Code " + zipCode);

        json branchLocatorReq = `{"BranchLocator": {"ZipCode":""}}`;
        json:set(branchLocatorReq, "$.BranchLocator.ZipCode", zipCode);
        message:setJsonPayload(backendServiceReq, branchLocatorReq);

        message response = http:HTTPConnector.post(branchLocatorService, "", backendServiceReq);

        json branchLocatorRes = message:getJsonPayload(response);

        string branchCode = json:getString(branchLocatorRes, "$.ABCBank.BranchCode");
        system:println("Branch Code " + branchCode);

        json bankInfoReq = `{"BranchInfo": {"BranchCode":""}}`;
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
        message response = {};

        json jsonRequest = message:getJsonPayload(m);
        string zipCode = json:getString(jsonRequest, "$.BranchLocator.ZipCode");

        json payload = {};
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
        message response = {};

        json jsonRequest = message:getJsonPayload(m);
        string branchCode = json:getString(jsonRequest, "$.BranchInfo.BranchCode");

        json payload = {};
        if (branchCode == "123") {
            payload = `{"ABC Bank": {"Address": "111 River Oaks Pkwy, San Jose, CA 95999"}}`;
        } else {
            payload = `{"ABC Bank": {"error": "No branches found."}}`;
        }

        message:setJsonPayload(response, payload);
        reply response;
    }
}


