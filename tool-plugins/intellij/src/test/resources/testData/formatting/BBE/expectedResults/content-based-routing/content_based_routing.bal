import ballerina/http;
import ballerina/log;
import ballerina/mime;

//Define the attributes associated with the client endpoint here.
endpoint http:Client locationEP {
    url: "http://www.mocky.io"
};

@http:ServiceConfig {
    basePath: "/cbr"
}
service<http:Service> contentBasedRouting bind { port: 9090 } {

    //Use `resourceConfig` annotation to declare the HTTP method.
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/route"
    }
    cbrResource(endpoint outboundEP, http:Request req) {
        //Get the JSON payload from the request message.
        var jsonMsg = req.getJsonPayload();

        match jsonMsg {
            json msg => {
                //Get the string value relevant to the key `name`.
                string nameString;

                nameString = check <string>msg["name"];
                (http:Response|error|()) clientResponse;

                if (nameString == "sanFrancisco") {
                    //Here, `post` represents the POST action of the HTTP client connector.
                    //This routes the payload to the relevant service when the server accepts the enclosed entity.
                    clientResponse =
                    locationEP->post("/v2/594e018c1100002811d6d39a", ());

                } else {
                    clientResponse =
                    locationEP->post("/v2/594e026c1100004011d6d39c", ());
                }
                //Use the native function 'respond' to send the client response back to the caller.
                match clientResponse {
                    http:Response respone => {
                        outboundEP->respond(respone) but { error e =>
                        log:printError("Error sending response", err = e) };
                    }
                    error conError => {
                        error err = {};
                        http:Response res = new;
                        res.statusCode = 500;
                        res.setPayload(err.message);
                        outboundEP->respond(res) but { error e =>
                        log:printError("Error sending response", err = e) };
                    }
                    () => {}
                }

            }
            error err => {
                http:Response res = new;
                res.statusCode = 500;
                res.setPayload(err.message);
                outboundEP->respond(res) but { error e =>
                log:printError("Error sending response", err = e) };
            }
        }
    }
}
