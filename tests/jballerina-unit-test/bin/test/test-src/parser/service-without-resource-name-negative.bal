import ballerina/http;

service HelloService on new http:MockListener(9090) {


  (http:Caller caller, http:Request request) {
  }
}
