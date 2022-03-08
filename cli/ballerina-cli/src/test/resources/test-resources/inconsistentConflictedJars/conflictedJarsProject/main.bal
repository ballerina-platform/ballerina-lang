import ballerina/io;
import pramodya/uberJarPackage as uber;
import pramodya/correctJarPackage as correct;

public function main() {
    io:println("Hello, World!");
    io:println(uber:hello("Uber"));
    io:println(correct:hello("Correct"));
}
