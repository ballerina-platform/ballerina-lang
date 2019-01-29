import ballerina/http;

type testRecord record {
    string /*def*/s;
}

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        testRecord ts = {/*ref*/s:""};
    }
}
