import ballerina/test;

any[] outputs = [];

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var entry in s {
        outputs.push(entry);
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    map<anydata> mp1 = {country: "Sri Lanka", "occupation": "Software Engineer"};
    test:assertEquals(outputs[0], "Name: ");
    test:assertEquals(outputs[1], "Peter");
    test:assertEquals(outputs[2], "Age: ");
    test:assertEquals(outputs[3], 28);
    test:assertEquals(outputs[4], "Other Details: ");
    test:assertEquals(outputs[5], mp1);
    test:assertEquals(outputs[6], "Name: ");
    test:assertEquals(outputs[7], "Peter");
    test:assertEquals(outputs[8], "Age: ");
    test:assertEquals(outputs[9], 28);
    test:assertEquals(outputs[10], "Country Name: ");
    test:assertEquals(outputs[11], "Sri Lanka");
    test:assertEquals(outputs[12], "Capital Name: ");
    test:assertEquals(outputs[13], "Colombo");
}
