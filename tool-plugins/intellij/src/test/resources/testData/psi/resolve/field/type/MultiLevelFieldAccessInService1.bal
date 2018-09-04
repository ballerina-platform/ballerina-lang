import ballerina/http;

type Name record {
    string firstName;
}
type Person record {
    Name /*def*/name;
}

service<http:Service> hello bind { port: 9090 }  {
    sayHello(endpoint caller, http:Request req) {
        Name name = {firstName:""};
        Person person = {/*ref*/name:name};
    }
}
