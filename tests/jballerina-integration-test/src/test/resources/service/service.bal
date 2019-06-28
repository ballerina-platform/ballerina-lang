import ballerina/http;


listener http:Listener helloWorldEP = new(9600);


@http:ServiceConfig {

    basePath: "/hello"

}

service helloWorld on helloWorldEP {


   @http:ResourceConfig {

       methods: ["GET"],

       path: "/{name}"

   }

   resource function sayHello(http:Caller caller, http:Request req, string name) {

       var result = caller->respond("Hello, World!, " + untaint name);

   }

}