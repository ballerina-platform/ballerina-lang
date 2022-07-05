
public client class MyMainClient {
    public string url;

    isolated function init(string url) returns error? {
        self.url = url;
    }

    remote isolated function getAll(@untainted string path) returns string|error? {
        return "Hello";
    }
}

MyMainClient myClient = new ("http://abcd");

public function main() returns error? {
    MyMainClient clientEndpoint = new ("http://postman-echo.com");
    var response = check clientEndpoint->getAll("");

    var df = check myClient->getAll("");
    var temp;
    temp = df;

    boolean status = deleteClient(clientEndpoint);
}

function deleteClient(MyMainClient mmc) returns boolean {
    return true;
}
