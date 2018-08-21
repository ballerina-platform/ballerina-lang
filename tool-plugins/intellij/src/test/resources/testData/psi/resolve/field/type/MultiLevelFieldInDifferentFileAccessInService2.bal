import ballerina/http;

service<http:Service> hello bind { port: 9090 }  {
    sayHello(endpoint caller, http:Request req) {
        Name name = {firstName:""};
        Person person = {};
        person./*ref*/name=name;
    }
}
