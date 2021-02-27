import ballerina/io;
import ballerina/http;
import ballerina/log;
import sammj/adder;

// adfadsfds
public function main(string[] args) returns error? {
    int a = args[0]; //adfadfsd
    int b = args[1];
    check adder:add(a, b);
}
