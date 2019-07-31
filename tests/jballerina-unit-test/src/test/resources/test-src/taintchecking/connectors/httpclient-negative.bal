import ballerina/http;

public function main (string... args) {
    http:Client clientEndpoint = new("https://postman-echo.com");
    string param = args[0];
    string headerName = args[0];
    string headerValue = args[0];

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, req);
    if (response is http:Response) {
        var msg = response.getTextPayload();
        if (msg is string) {
            secureFunction(msg, msg);
        } else {
            error err = msg;
            panic err;
        }
    } else {
        error err = response;
        panic err;
    }
}

function secureFunction (@untainted string secureIn, string insecureIn) {
}
