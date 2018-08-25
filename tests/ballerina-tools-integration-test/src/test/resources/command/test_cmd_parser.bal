import ballerina/io;

function main(string... args) {
    if (args[0] == "") {
        io:println("empty_string");
    } else {
        io:println(args[0]);
    }
}

