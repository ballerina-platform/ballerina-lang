public client class MyMainClient {
    public string url;

    isolated function init(string url) returns error? {
        self.url = url;
    }

    remote isolated function getAll(@untainted string path) returns string|error? {
        return "Hello";
    }
}
