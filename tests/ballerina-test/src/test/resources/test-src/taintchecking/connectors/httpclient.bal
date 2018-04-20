import ballerina/http;

endpoint http:Client clientEndpoint {
    url: "https://postman-echo.com"
};

function main (string... args) {
    string param = "staticValue";
    string headerName = "staticValue";
    string headerValue = "staticValue";

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, req);
    match response {
        http:Response resp => {
            var msg = resp.getStringPayload();
            match msg {
                string stringPayload => {
                    normalFunction (stringPayload);
                }
                http:PayloadError payloadError => return;
            }
        }
        http:HttpConnectorError err => return;
    }
}

function normalFunction (string insecureIn) {
}
