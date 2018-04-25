import ballerina/io;

function main(string... args) {
    string name = "Ballerina";
    // This creates a string template.
    string template = string `Hello {{name}}!!!`;
    io:println(template);
}
