import ballerina/http; 

@http:ServiceConfig {
   basePath:"/hello"
}
service myService1 on new http:Listener(9090) {
   @http:ResourceConfig {
       methods:["GET"],
       path:"/sayHello"
   }
   resource function foo(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello");
   }
}
