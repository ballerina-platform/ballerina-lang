import ballerina/http;
import ballerina/io;

service foo on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}

public function main(string... args) {
    typedesc<any> t = typeof foo;
    io:println(t.@http:)
}