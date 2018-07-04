import ballerina/http;
import ballerina/log;
import ballerina/mime;

// Consumes and produces annotations that contain MIME types as an array of strings.
service<http:Service> infoService bind { port: 9092 } {

    // The resource can consume/accept `text/json` and `application/json` media types only. Therefore, the `Content-Type` header must have one of the types.
    // The resource can produce `application/xml` payloads. Therefore, the `Accept` header should be set accordingly.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/",
        consumes: ["text/json", "application/json"],
        produces: ["application/xml"]
    }
    student(endpoint caller, http:Request req) {
        // Get JSON payload from the request message.
        http:Response res = new;
        var msg = req.getJsonPayload();
        match msg {
            json jsonMsg => {
                // Get the string value that is relevant to the key "name".
                string nameString = check <string>jsonMsg["name"];
                if (check nameString.matches("[a-zA-Z]+")) {
                    // Create XML payload and send back a response.
                    xml name = xml `<name>{{untaint nameString}}</name>`;
                    res.setXmlPayload(name);
                } else {
                    res.statusCode = 400;
                    res.setPayload("Name contains invalid data");
                }
            }
            error err => {
                res.statusCode = 500;
                res.setPayload(untaint err.message);
            }
        }
        caller->respond(res) but { error e => log:printError("Error in responding", err = e) };
    }
}
