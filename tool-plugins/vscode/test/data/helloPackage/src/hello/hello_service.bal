import ballerina/http; 
import ballerina/io;

@http:ServiceConfig {
   basePath:"/hello"
}
service myService1 on new http:Listener(9090) {
   @http:ResourceConfig {
       methods:["GET"],
       path:"/sayHello"
   }
   resource function foo(http:Caller caller, http:Request req) {
       io:println("request received");
       checkpanic caller->respond("Hello");
   }
}
