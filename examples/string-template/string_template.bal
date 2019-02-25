import ballerina/io;

public function main() {
    string name = "Ballerina";
    // This creates a `string` template.
    string template = string `Hello ${name}!!!`;
    // This prints the string template defined.
    io:println(template);
}
