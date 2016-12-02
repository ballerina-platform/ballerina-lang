package ballerina.net.http;

connector HttpConnector (string uri, map options) {

    // blocking calls
    native action get(HttpConnector h, string path, message m) (message) throws exception;
    native action put(HttpConnector h, string path, message m) (message) throws exception;
    native action post(HttpConnector h, string path, message m) (message) throws exception;
    native action delete(HttpConnector h, string path, message m) (message) throws exception;
    native action executeMethod(HttpConnector h, string httpVerb, string path, message m) (message) throws exception;

    // non-blocking method initiation
    native action sendGet(HttpConnector h, string path) (int) throws exception;
    native action sendPost(HttpConnector h, string path) (int) throws exception;
    native action sendPut(HttpConnector h, string path) (int) throws exception;
    native action sendDelete(HttpConnector h, string path) (int) throws exception;
    native action sendMethod(HttpConnector h, string httpVerb, string path, message m) (int) throws exception;

    // receive a non-blocking response
    native action receiveResponse(HttpConnector h, int key) (message) throws exception;
}
