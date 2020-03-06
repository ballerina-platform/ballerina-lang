import ballerina/test;

any[] outputs = [];
int counter = 0;

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
    // Invoking the main function
    main();

    xml someText = xml `Hello, World!`;
    xml book = xml `<book/>`;
    string itemType = "comment";
    string nameOfElem = "name";
    string txtVal = "Book1";
    boolean isEmpty = false;
    boolean isSingleton = false;
    xml bookComment = xml `<!--some comment-->`;
    xml bookName = xml `<name>Book1</name>`;
    xml content = someText + bookName + bookComment;
    book.setChildren(content);

    test:assertEquals(outputs[0], itemType);
    test:assertEquals(outputs[1], nameOfElem);
    test:assertEquals(outputs[2], txtVal);
    test:assertEquals(outputs[3], isEmpty);
    test:assertEquals(outputs[4], isSingleton);
    test:assertEquals(outputs[5], bookComment);
    test:assertEquals(outputs[6], bookName);
    test:assertEquals(outputs[7], bookName);
    test:assertEquals(outputs[8], book);
    test:assertEquals(outputs[9], book/*);
    test:assertEquals(outputs[10], bookName);
    test:assertEquals(outputs[11], content.strip());
    test:assertEquals(outputs[12], bookComment.copy());
}
