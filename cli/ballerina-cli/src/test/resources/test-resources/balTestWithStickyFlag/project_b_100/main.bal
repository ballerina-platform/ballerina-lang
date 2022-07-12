import pramjs/pkg_a;
import ballerina/io;

public function main(string a) {
    io:println(helloFunction());
}

public function helloFunction() returns string {
    return pkg_a:hello("Sri Lanka");
}
