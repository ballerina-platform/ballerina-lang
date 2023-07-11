# + url - Target service url
public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }
    
    # Sample resource method.
    #
    # + id1 - Path parameter
    # + ids - Rest path parameter
    # + str - Argument
    # + ids2 - Rest argument
    # + return - The response for the request
    resource function post path1/[string id1]/path2/[string... ids](string str, string... ids2) returns Response {
        return new Response();
    }
}

public class Response {
    public int statusCode = 200;
    public string reasonPhrase = "Test Reason phrase";
}
