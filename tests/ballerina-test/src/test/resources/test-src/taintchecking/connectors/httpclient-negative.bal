import ballerina/http;

endpoint http:Client clientEndpoint {
    url: "https://postman-echo.com"
};

function main (string... args) {
    string param = args[0];
    string headerName = args[0];
    string headerValue = args[0];

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, req);
    match response {
        http:Response resp => {
            var msg = resp.getStringPayload();
            match msg {
                string stringPayload => {
                    secureFunction (stringPayload, stringPayload);
                }
                http:PayloadError payloadError => return;
            }
        }
        http:HttpConnectorError err => return;
    }
}

function secureFunction (@sensitive string secureIn, string insecureIn) {
}
