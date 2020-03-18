import ballerina/config;
import ballerina/http;
import ballerina/log;

http:ClientConfiguration weatherEPConfig = {
    followRedirects: {enabled: true, maxCount: 5},
    secureSocket: {
        trustStore: {
            path: config:getAsString("b7a.home") +
                        "/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

//Service is invoked using `basePath` value "/hbr".
@http:ServiceConfig {
    basePath: "/hbr"
}

service headerBasedRouting on new http:Listener(9090) {
    // `http:resourceConfig{}` annotation with 'GET' method declares the
    // HTTP method.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/route"
    }

    resource function hbrResource(http:Caller caller, http:Request req) {
        http:Client weatherEP = new ("http://samples.openweathermap.org",
                                     weatherEPConfig);
        http:Client locationEP = new ("http://www.mocky.io");
        // Create a new outbound request to handle client call.
        http:Request newRequest = new;
        // Check whether `x-type` header exists in the request.
        if (!req.hasHeader("x-type")) {
            http:Response errorResponse = new;
            errorResponse.statusCode = 500;
            json errMsg = {"error": "'x-type' header is not found"};
            errorResponse.setPayload(errMsg);

            var result = caller->respond(errorResponse);

            if (result is error) {
                log:printError("Error sending response", result);
            }
            return;
        }
        //`getHeader()` returns header value of the specified header name.
        string nameString = req.getHeader("x-type");

        http:Response|error response;
        if (nameString == "location") {
            //`post()` remote function represents the 'POST' operation
            // of the HTTP client.
            // Route payload to the relevant service.
            response = locationEP->post("/v2/5adddd66300000bd2a4b2912",
                                        newRequest);

        } else {
            //`get()` remote function can be used to make an http GET call.
            response =
                weatherEP->get("/data/2.5/weather?lat=35&lon=139&appid=b1b1",
                                 newRequest);

        }

        if (response is http:Response) {
            // `respond()` sends back the inbound clientResponse to the caller
            // if no error occurs.

            var result = caller->respond(response);

            if (result is error) {
                log:printError("Error sending response", result);
            }

        } else {
            http:Response errorResponse = new;
            errorResponse.statusCode = 500;
            errorResponse.setPayload(<string>response.detail()?.message);
            var result = caller->respond(errorResponse);

            if (result is error) {
                log:printError("Error sending response", result);
            }
        }
    }
}
