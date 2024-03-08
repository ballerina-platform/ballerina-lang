import ballerina/http;
import project2.mymath;

service / on new http:Listener(9090) {

    resource function get greeting() returns string {
        return "Hello, World!";
    }

    resource function get add(int a, int b) returns int {
        return mymath:add(a, b);
    }
    
    resource function get subtract(int a, int b) returns int {
        return mymath:subtract(a, b);
    }
    
}