public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }

    remote function post(@untainted string path, RequestMessage message, string targetType="xml") returns Response {
        Response response = new();
        return response;
    }
    
    resource function post path1/[string id1]/path2/[string... ids](string str, string... ids2) returns Response {
        return new Response();
    }
}

public class Response {
    public int statusCode = 200;
    public string reasonPhrase = "Test Reason phrase";
}

public type RequestMessage string|xml|json|byte[]|();


class TestClass {
    private Client testClient;
    private boolean testField = false;

    function init(Client testClient) {
        self.testClient = testClient;
    }
}

public function test() {
    Client cl = new ("");
    string s = "path";
    int i = 10;

    Response response = cl -> /path1/[""]/path2/[""].post("");

}
