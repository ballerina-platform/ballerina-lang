import ballerina/io;
import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function, which replaces the real function
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();
    xml xml1 = xml `<book>The Lost World</book>`;
    xml xml2 = xml `Hello, world!`;
    xml xml3 = xml `<!--I am a comment-->`;
    xml xml4 = xml `<?target data?>`;
    xml xml5 = xml1 + xml2 + xml3 + xml4;

    test:assertEquals(xml1, outputs[0]);
    test:assertEquals(xml2, outputs[1]);
    test:assertEquals(xml3, outputs[2]);
    test:assertEquals(xml4, outputs[3]);
    test:assertEquals(xml5, outputs[5]);
}
