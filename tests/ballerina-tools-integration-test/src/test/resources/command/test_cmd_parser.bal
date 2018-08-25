import ballerina/io;

public function main(string... args) {
    if (args[0] == "") {
        io:println("empty_string");
    } else {
        io:println(args[0]);
    }
}

