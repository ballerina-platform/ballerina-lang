import ballerina/io;

public function main() {
    // String Sequence Type Descriptor
    string s1 = "Ballerina ";
    string s2 = "\u{0633}";

    // XML Sequence Type Descriptor
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
}
