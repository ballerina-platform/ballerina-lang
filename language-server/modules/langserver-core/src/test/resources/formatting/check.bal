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

function scale3(string num) returns int | error {
    int x =check    start   parse(num);
    int y = 0;
    y=check    parse(num) ;
    return x * 10;
}

function scale4(string num) returns int | error {
    int x =check
             start        parse(num);
    int y = 0;
    y = check
                  parse(num);
    return x * 10;
}

function scale5(string num) returns int | error {
    int x =
             check
start
         parse(num)
;
    int y = 0;
    y =
check
         parse(num)
  ;
    return x * 10;
}

public function test1() returns string | error {
    return "";
}

public function test2() returns error? {
    string sd = check name();
    io:println("Response from organization count query from DB: " +check test1());
}