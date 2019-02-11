import ballerina/http;

http:Client clientEP = new("http://www.mocky.io");

function testSignatureHelp () {
    http:Request req = new;
    json connectionJson = { protocol: "json" };
    req.setTextPayload("Hello", contentType = "text/plain");
    (http:Response|error) response = clientEP->get("/path", message = req);
}
