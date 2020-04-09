import ballerina/test;

string[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0].toString();
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "name=John Doe age=17 grades=maths=80 physics=75 chemistry=65");
    test:assertEquals(outputs[1], "John Doe");
    test:assertEquals(outputs[2], "John Doe");
    test:assertEquals(outputs[3], "80");
    test:assertEquals(outputs[4], "name=Peter age=16 grades=maths=40 physics=35 chemistry=35");
    test:assertEquals(outputs[5], "name=John Doe age=17 grades=maths=80 physics=75 chemistry=65");
    test:assertEquals(outputs[6], "name=Peter age=16 grades=maths=40 physics=35 chemistry=35 address=city=Colombo country=Sri Lanka");
    test:assertEquals(outputs[7], "maths=80 physics=75 chemistry=65 english=90");
    test:assertEquals(outputs[8], "90");
}
