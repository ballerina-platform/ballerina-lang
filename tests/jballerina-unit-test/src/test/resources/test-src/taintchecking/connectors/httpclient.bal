import ballerina/http;

public function main (string... args) {
    http:Client clientEndpoint = new("https://postman-echo.com");
    string param = "staticValue";
    string headerName = "staticValue";
    string headerValue = "staticValue";

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, req);
    if (response is http:Response) {
        var msg = response.getTextPayload();
        if (msg is string) {
            normalFunction(msg);
        } else {
            error err = msg;
            panic err;
        }
    } else {
        error err = response;
        panic err;
    }
}

function normalFunction (string insecureIn) {
}
