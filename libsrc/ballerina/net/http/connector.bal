package ballerina.net.http;

connector HttpConnector (string serviceUri, map options) {
    native action get(HttpConnector h, string path, message m) (message) throws exception;
    native action put(HttpConnector h, string path, message m) (message) throws exception;
    native action post(HttpConnector h, string path, message m) (message) throws exception;
    native action patch(HttpConnector h, string path, message m) (message) throws exception;
    native action delete(HttpConnector h, string path, message m) (message) throws exception;
    native action execute(HttpConnector h, string httpVerb, string path, message m) (message) throws exception;
}
