import ballerina/io;

function main (string... args) {

    string name = "Ballerina";
    // Create a string template.
    string template = string `Hello {{name}}!!!`;
    // Lets print the final string value of the template.
    io:println(template);
}
