import ballerina/io;

public function main() {
    var regexr = re `^test all\s*(?:resources?|endpoints?|paths?)?\.?$`;
    string str1 = "test all resource";
    io:println(str1.matches(regexr)); // true
}
