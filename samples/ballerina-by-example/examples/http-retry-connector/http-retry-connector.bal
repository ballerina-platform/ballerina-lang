import ballerina.net.http;

function main (string[] args) {
    endpoint<http:ClientConnector> httpEndpoint {}
    // Create an HTTP Client Connector.
    http:ClientConnector clientCon = create http:ClientConnector(
                                                "https://postman-echo.com", {});
    // Create an HTTP Retry Connector.
    http:RetryClient retryCon = create http:RetryClient(clientCon, 5, 3000);
    // Bind Retry Connector with endpoint.
    bind retryCon with httpEndpoint;
    http:Request req = {};

    http:Response resp;
    // Send a GET request to the specified endpoint.
    resp, _ = httpEndpoint.get("/get?test=123", req);
    println("GET request:");
    println(resp.getJsonPayload());
}
