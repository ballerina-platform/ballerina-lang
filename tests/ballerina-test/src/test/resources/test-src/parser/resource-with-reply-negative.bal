import ballerina/http;

service<http:Service> echo1 {

passthru (endpoint caller, http:Request request) {
    reply;
  }
}
