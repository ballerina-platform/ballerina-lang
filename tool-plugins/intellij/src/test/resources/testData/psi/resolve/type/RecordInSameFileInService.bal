import ballerina/http;

type /*def*/testRecord record {
    string a;
}

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        /*ref*/testRecord ts;
    }
}
