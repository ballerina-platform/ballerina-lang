import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/xmlparser"
}
service<http:Service> xmlParserService bind { port: 9090 } {

    @http:ResourceConfig {
        path: "/"
    }
    parse(endpoint caller, http:Request request) {
        var payload = request.getXmlPayload();
        if (payload is xml) {
            var responseToCaller = caller->respond(untaint payload.getTextValue());
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setTextPayload(untaint payload.reason());
            var responseToCaller = caller->respond(res);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}
