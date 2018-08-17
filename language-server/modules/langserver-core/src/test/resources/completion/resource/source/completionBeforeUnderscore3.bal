import ballerina/http;

service<http:Service> hello bind {port:9090} {
   @http:ResourceConfig {
       path: "/",
       methods: ["POST"]
       
   }
   sayHello (endpoint caller, http:Request request) {
       http:Response res = new;
       h
       _ = caller->respond(res);
   }
}