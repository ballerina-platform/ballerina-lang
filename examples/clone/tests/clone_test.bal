import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach any a in s {
        outputs[counter] = a;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Define an `Address` record.
    Address address = {
        country : "USA",
        state: "NC",
        city: "Raleigh",
        street: "Daniels St"
    };

    // Define a `Person` record.
    Person person = {
        name: "Alex",
        age: 24,
        married: false,
        salary: 8000.0,
        address: address
    };

    // Invoking the main function.
    main();
    test:assertEquals(outputs[0], "Source value: ");
    test:assertEquals(outputs[1], person);
    test:assertEquals(outputs[2], "Cloned value: ");
    test:assertEquals(outputs[3], person);
    test:assertEquals(outputs[4], "Source and Clone are at two different memory locations: ");
    test:assertEquals(outputs[5], true);
}
