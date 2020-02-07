import ballerina/http;
import ballerina/io;

service serviceName on new http:Listener(8080) {
   @http:ResourceConfig {
       path: "/",
       methods: ["POST"]
       
   }
   resource function newResource(http:Caller caller, http:Request request) {
       http:Response res = new;
       io:
       checkpanic caller->respond(res);
   }
}