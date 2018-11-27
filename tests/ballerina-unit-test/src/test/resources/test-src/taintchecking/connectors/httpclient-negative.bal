import ballerina/http;

public function main (string... args) {
    http:Client clientEndpoint = new("https://postman-echo.com");
    string param = args[0];
    string headerName = args[0];
    string headerValue = args[0];

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, message = req);
    if (response is http:Response) {
        var msg = response.getTextPayload();
        if (msg is string) {
            secureFunction(msg, msg);
        } else {
            panic msg;
        }
    } else {
        panic response;
    }
}

function secureFunction (@sensitive string secureIn, string insecureIn) {
}
