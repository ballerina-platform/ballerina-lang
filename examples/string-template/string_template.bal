import ballerina/io;

public function main() {
    string name = "Ballerina";
    // This creates a `string` template.
    string template = string `Hello ${name}!!!`;
    // This prints the defined string template.
    io:println(template);
}
