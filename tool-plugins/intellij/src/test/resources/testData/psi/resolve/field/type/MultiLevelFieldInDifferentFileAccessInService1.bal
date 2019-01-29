import ballerina/http;

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        Name name = {firstName:""};
        Person person = {/*ref*/name:name};
    }
}
