import ballerina/http;

type Name record {
    string firstName;
}
type Person record {
    Name /*def*/name;
}

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        Name name = {firstName:""};
        Person person = {/*ref*/name:name};
    }
}
