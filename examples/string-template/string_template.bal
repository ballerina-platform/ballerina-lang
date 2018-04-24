import ballerina/io;

function main(string... args) {
    string name = "Ballerina";
    // Here's how you can create a string template.
    string template = string `Hello {{name}}!!!`;
    io:println(template);
}
