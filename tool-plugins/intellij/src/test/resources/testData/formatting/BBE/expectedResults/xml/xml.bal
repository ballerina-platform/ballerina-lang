import ballerina/io;

function main(string... args) {
    // The XML element. There can only be one root element.
    xml x1 = xml `<book>The Lost World</book>`;
    io:println(x1);

    // The XML text.
    xml x2 = xml `Hello, world!`;
    io:println(x2);

    // The XML comment.
    xml x3 = xml `<!--I am a comment-->`;
    io:println(x3);

    // The XML processing instruction.
    xml x4 = xml `<?target data?>`;
    io:println(x4);

    // Multiple XML items can be combined to form a sequence of XML. The resulting sequence is another XML on its own.
    xml x5 = x1 + x2 + x3 + x4;
    io:println("\nResulting XML sequence:");
    io:println(x5);
}
