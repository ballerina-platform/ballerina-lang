import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/xmlparser"
}
service xmlParserService on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/"
    }
    resource function parse(http:Caller caller, http:Request request) {
        var payload = request.getXmlPayload();
        if (payload is xml) {
            var responseToCaller = caller->respond(untaint payload.getTextValue());
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setTextPayload(untaint payload.reason());
            var responseToCaller = caller->respond(res);
            if (responseToCaller is error) {
                log:printError("Error sending response", responseToCaller);
            }
        }
    }
}
