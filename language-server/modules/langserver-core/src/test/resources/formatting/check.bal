import ballerina/http;

function parse(string num) returns int|error {
    return int.convert(num);
}

function scale1(string num) returns int|error {
    int x =check   parse(num);
    int y = 0;
    y =check parse(num);
    return x * 10;
}

function scale2(string num) returns int|error {
    int x =
        check   parse(num);
    return x * 10;
}

@http:ServiceConfig {
   basePath:"/hello"
}
service myService1 on new http:Listener(9090) {
   @http:ResourceConfig {
       methods:["GET"],
       path:"/sayHello"
   }
   resource function foo(http:Caller caller, http:Request req) returns error? {check caller->respond("Hello");
   }
}

@http:ServiceConfig {
   basePath: "/hello2"
}
service myService2 on new http:Listener(9090) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    resource function foo(http:Caller caller, http:Request req) returns error? {
            check caller->respond("Hello");
    }
}