import ballerina/http;
import ballerina/log;

service infoService on new http:Listener(9092) {
    // The `consumes` and `produces` annotations contain MIME types as an
    // array of strings. The resource can consume/accept `text/json` and
    // `application/json` media types only. Therefore, the `Content-Type` header
    // of the request must be in one of these two types. The resource can produce
    // `application/xml` payloads. Therefore, you need to set the `Accept` header accordingly.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/",
        consumes: ["text/json", "application/json"],
        produces: ["application/xml"]
    }
    resource function student(http:Caller caller, http:Request req) {
        // Get JSON payload from the request message.
        http:Response res = new;
        var msg = req.getJsonPayload();
        if (msg is json) {
            // Get the `string` value that is relevant to the key "name".
            string nameString = <string>msg.name;
            // Create XML payload and send back a response.
            xml name = xml `<name>{{untaint nameString}}</name>`;
            res.setXmlPayload(name);
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted> <string>msg.detail()?.message);
        }

        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error in responding", err = result);
        }
    }
}
