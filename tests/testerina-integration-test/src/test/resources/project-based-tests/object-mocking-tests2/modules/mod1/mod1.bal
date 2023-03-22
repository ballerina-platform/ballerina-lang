public client class HttpClient {
    remote isolated function get(string path) returns string {
        return "hello from REMOTE method";
    }

    resource function get [string ...path]() returns string {
        return "hello from RESOURCE method";
    }
}
