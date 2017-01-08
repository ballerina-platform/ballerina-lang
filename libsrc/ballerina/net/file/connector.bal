package ballerina.net.file;

connector FileConnector (string serviceUri, map options) {
    native action send(HttpConnector h, string path, message m) (message) throws exception;
}