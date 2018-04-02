import ballerina/http;

service<http:Service> echo1 {

passthru (endpoint outboundEP, http:Request request) {
    reply;
  }
}
