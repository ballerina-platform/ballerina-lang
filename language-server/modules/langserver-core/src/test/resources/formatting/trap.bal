import ballerina/runtime;
import ballerina/io;
import ballerina/http;
import ballerina/config;

function add(int|error value) {

}

function divide(int a, int b) returns int {
    return a / b;
}

function substract(int a, int b) {
    int | error result =
        trap
divide (1, 0);
           add (
trap
         divide (1, 0)
       );
}

public function main() {
    int|error result =    trap    divide  ( 1 ,  0)  ;
    add(  trap  divide(1, 0));
    if (result is int) {
        io:println("int result: " + result);
    } else {
        io:println("Error occurred: ", result.reason());
    }
}