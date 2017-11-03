import ballerina.net.http;
function main (string[] args) {
    testConnectors();
}
function testConnectors() {
    endpoint <HTTPClient> endpoint1{
    }
    HTTPClient httpC = create HTTPClient("http://localhost:9090");
    bind httpC with endpoint1;
}

connector HTTPClient(string url) {
    action get(string path) (http:Response) {
        endpoint <http:ClientConnector> endpoint1{
            create http:ClientConnector(url, {});
        }
	http:Request req = {};
        return endpoint1.get(path, req);
    }
}