import ballerina/io;

service class Person {
    resource function get age() returns int {
        do {
        } on fail error err {
            //handle error
        }
    }
}