import ballerina/io;
import math;

public function printMyName() {
    io:println("Hello MyName!!!");
    string s = math:joinStrings();
    io:println(s);
}
