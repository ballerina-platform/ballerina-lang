
import ballerina/io;
int a1 = 0;

public function getA1() returns int {
    return a1;
}

public function updateA1(int val) returns int {
    a1 = a1 + val;
    return a1;
}


int b = temp();

function temp() returns int {
    io:println("PackageA");
    return 9;
}