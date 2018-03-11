import ballerina/net.http;

function testConnectorCast() (http:HttpClient, error) {
    any client = create http:HttpClient("http://localhost:9090", {});
    http:HttpClient castClient;
    error e;
    castClient, e = (http:HttpClient)client;
    return castClient, e;
}
