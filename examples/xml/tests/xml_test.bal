import ballerina/io;
import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function, which replaces the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
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

    test:assertEquals(outputs[0], xml1);
    test:assertEquals(outputs[1], xml2);
    test:assertEquals(outputs[2], xml3);
    test:assertEquals(outputs[3], xml4);
    test:assertEquals(outputs[5], xml5);
}
