package servicechaining.samples;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.jsons;

@http:BasePath{value: "/ABCBank"}
service ATMLocator {

    @http:POST{}
    @http:Path{value: "/locator"}
    resource locator() {
        http:ClientConnector bankInfoService = create http:ClientConnector("http://localhost:9090/bankinfo");
        http:ClientConnector branchLocatorService = create http:ClientConnector("http://localhost:9090/branchlocator");
        message backendServiceReq = {};
        json jsonLocatorReq = messages:getJsonPayload(undefined);
        string zipCode = jsons:getString(undefined, "$.ATMLocator.ZipCode");
        system:println("Zip Code " + undefined);
        json branchLocatorReq = {"BranchLocator":{"ZipCode":""}};
        jsons:set(undefined, "$.BranchLocator.ZipCode", undefined);
        messages:setJsonPayload(undefined, undefined);
        message response = http:ClientConnector.post("", undefined);
        json branchLocatorRes = messages:getJsonPayload(undefined);
        string branchCode = jsons:getString(undefined, "$.ABCBank.BranchCode");
        system:println("Branch Code " + undefined);
        json bankInfoReq = {"BranchInfo":{"BranchCode":""}};
        jsons:set(undefined, "$.BranchInfo.BranchCode", undefined);
        messages:setJsonPayload(undefined, undefined);
        undefined = http:ClientConnector.post("", undefined);

        reply response;
    }
}
