import ballerina/http;
import ballerina/log;

http:ClientEndpointConfig weatherEPConfig = {
    followRedirects: { enabled: true, maxCount: 5 }
};

//Service is invoked using `basePath` value "/hbr".
@http:ServiceConfig {
    basePath: "/hbr"
}

service headerBasedRouting on new http:Listener(9090) {
    //`http:resourceConfig{}` annotation with GET method declares the HTTP method.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/route"
    }

    resource function hbrResource(http:Caller caller, http:Request req) {
        http:Client weatherEP = new("http://samples.openweathermap.org",
                                    config = weatherEPConfig);
        http:Client locationEP = new("http://www.mocky.io");
        //Create new outbound request to handle client call.
        http:Request newRequest = new;
        // Checks whether 'x-type' header exists in the request.
        if (!req.hasHeader("x-type")) {
            http:Response errorResponse = new;
            errorResponse.statusCode = 500;
            json errMsg = { "error": "'x-type' header is not found" };
            errorResponse.setPayload(errMsg);

            var result = caller->respond(errorResponse);

            if (result is error){
                log:printError("Error sending response", err = result);
            }
            return;
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

        if (response is http:Response) {
            // `respond()` sends back the inbound clientResponse to the caller if no any error is found.

            var result = caller->respond(response);

            if (result is error){
                log:printError("Error sending response", err = result);
            }

        } else if (response is error) {
            http:Response errorResponse = new;
            errorResponse.statusCode = 500;
            errorResponse.setPayload(<string> response.detail().message);
            var result = caller->respond(errorResponse);

            if (result is error){
                log:printError("Error sending response", err = result);
            }
        }
    }
}
