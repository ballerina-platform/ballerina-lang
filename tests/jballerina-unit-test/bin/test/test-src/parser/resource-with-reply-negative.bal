import ballerina/http;

service echo1 on new http:MockListener(9090) {

resource function passthru (http:Caller caller, http:Request request) {
    reply;
  }
}
