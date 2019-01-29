import ballerina/http;

public function main (string... args) {
    http:Client clientEndpoint = new("https://postman-echo.com");
    string param = "staticValue";
    string headerName = "staticValue";
    string headerValue = "staticValue";

    http:Request req = new;
    req.setHeader(headerName, headerValue);

    var response = clientEndpoint -> get("/get?test=" + param, message = req);
    if (response is http:Response) {
        var msg = response.getTextPayload();
        if (msg is string) {
            normalFunction(msg);
        } else {
            panic msg;
        }
    } else {
        panic response;
    }
}

function normalFunction (string insecureIn) {
}
