import ballerina/http;
import ballerina/log;

endpoint http:Client locationEP {
    url: "http://www.mocky.io"
};

endpoint http:Client weatherEP {
    url: "http://samples.openweathermap.org"
};

//Service is invoked using `basePath` value "/hbr".
@http:ServiceConfig {
    basePath: "/hbr"
}

service<http:Service> headerBasedRouting bind { port: 9090 } {
    //`http:resourceConfig{}` annotation with GET method declares the HTTP method.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/route"
    }

    hbrResource(endpoint caller, http:Request req) {
        //Create new outbound request to handle client call.
        http:Request newRequest = new;
        // Checks whether 'x-type' header exists in the request.
        if (!req.hasHeader("x-type")) {
            http:Response errorResponse = new;
            errorResponse.statusCode = 500;
            json errMsg = { "error": "'x-type' header is not found" };
            errorResponse.setPayload(errMsg);
            caller->respond(errorResponse) but {
                error e => log:printError("Error sending response", err = e) };
            done;
        }
        //`getHeader()` returns header value of a specified header name.
        string nameString = req.getHeader("x-type");

        http:Response|error response;
        if (nameString == "location") {
            //`post()` represent the POST action of HTTP connector. Route payload to relevant service.
            response = locationEP->post("/v2/5adddd66300000bd2a4b2912",
                                        newRequest);

        } else {
            //`get()` action can be used to make http GET call.
            response =
                weatherEP->get("/data/2.5/weather?lat=35&lon=139&appid=b1b1",
                                message = newRequest);

        }

        match response {
            http:Response clientResponse => {
                //`respond()` sends back the inbound clientResponse to the caller if no any error is found.
                caller->respond(clientResponse)
                    but { error e => log:printError(
                                 "Error sending response", err = e) };

            }
            error err => {
                http:Response errorResponse = new;
                errorResponse.statusCode = 500;
                errorResponse.setPayload(err.message);
                caller->respond(errorResponse)
                    but { error e => log:printError(
                                 "Error sending response", err = e) };
            }
        }
    }
}
