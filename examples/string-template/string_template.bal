import ballerina/io;

public function main() {
    string name = "Ballerina";
    // Create a `string` template, embedding the `name` variable.
    string template = string `Hello ${name}!!!`;
    // Print the defined string value.
    io:println(template);
}
