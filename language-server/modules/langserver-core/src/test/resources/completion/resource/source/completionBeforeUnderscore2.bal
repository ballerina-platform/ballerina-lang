import ballerina/http;

service serviceName on new http:Listener(8080) {
   @http:ResourceConfig {
       path: "/",
       methods: ["POST"]
       
   }
   resource function newResource(http:Caller caller, http:Request request) {
       http:Response res = new;
       request.
       checkpanic caller->respond(res);
   }
}