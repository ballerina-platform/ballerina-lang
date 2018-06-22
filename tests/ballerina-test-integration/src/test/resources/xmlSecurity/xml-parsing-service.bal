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
        http:Response res = new;
        match (payload) {
            xml xmlPayload => {
                res.statusCode = 200;
                res.setTextPayload(untaint xmlPayload.getTextValue());
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error err => {
                res.statusCode = 500;
                res.setTextPayload(untaint err.message);
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}
