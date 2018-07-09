import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

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

    test:assertEquals(itemType, outputs[0]);
    test:assertEquals(nameOfElem, outputs[1]);
    test:assertEquals(txtVal, outputs[2]);
    test:assertEquals(isEmpty, outputs[3]);
    test:assertEquals(isSingleton, outputs[4]);
    test:assertEquals(bookComment, outputs[5]);
    test:assertEquals(bookName, outputs[6]);
    test:assertEquals(bookName, outputs[7]);
    test:assertEquals(book, outputs[8]);
    test:assertEquals(book.*, outputs[9]);
    test:assertEquals(bookName, outputs[10]);
    test:assertEquals(content.strip(), outputs[11]);
    test:assertEquals(bookComment.copy(), outputs[12]);
}
