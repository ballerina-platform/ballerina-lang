import ballerina/http;

listener http:Listener ep1 = new(9090);
listener http:Listener ep2 = new(9090);




service myService1 on new http: {
   resource function foo(http:Caller caller, http:Request req) {
       checkpanic caller->respond("Hello");
   }
}
