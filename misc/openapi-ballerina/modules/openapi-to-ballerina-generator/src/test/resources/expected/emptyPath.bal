import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    title: "serviceName",
    serviceVersion: "1.0.0"
}
@http:ServiceConfig {
    basePath: "/"
}
service serviceName on ep0 {
    resource function user (http:Caller outboundEp, http:Request _userReq) {
        http:Response _userRes = new;
        string _userPayload = "Sample user Response";
        _userRes.setTextPayload(_userPayload);
        var result = outboundEp->respond(_userRes);
        if (result is error) {
            log:printError(result.reason(), err = result);
        }
    }

}
