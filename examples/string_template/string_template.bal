import ballerina/io;

function main(string... args) {
    string name = "Ballerina";
    // Create a string template.
    string template = string `Hello {{name}}!!!`;
    // Let's print the final string value of the template.
    io:println(template);
}
