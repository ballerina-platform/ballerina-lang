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

    string nameOfElem = "name";
    string txtVal = "Hello, World!<name>Book1</name><!--some comment-->";
    string trueVal = "true"
    string commentVal = "<!--some comment-->";
    string nameElemVal = "<name>Book1</name>";
    string afterSetChildren = "<book>Hello, World!<name>Book1</name><!--some comment--></book>";
    string striped = "Hello, World!<name>Book1</name>";

    test:assertEquals(outputs[0], nameOfElem);
    test:assertEquals(outputs[2], txtVal);
    test:assertEquals(outputs[3], trueVal);
    test:assertEquals(outputs[4], "3");
    test:assertEquals(outputs[5], commentVal);
    test:assertEquals(outputs[6], nameElemVal);
    test:assertEquals(outputs[7], afterSetChildren);
    test:assertEquals(outputs[8], striped);
}
