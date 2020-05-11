import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    foreach var str in s {
    outputs[counter] = str;
    counter += 1;
    }
    
}

@test:Config {}
function testFunc() {
    Person p = {
        name: "John",
        age: 30,
        addr: {
            line01: "No. 61",
            line02: "Brandon street",
            city: "Santa Clara",
            state: "CA",
            zipcode: "95134"
        }
    };

    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "Age: ");
    test:assertEquals(outputs[1], ());
    test:assertEquals(outputs[2], "Age: ");
    test:assertEquals(outputs[3], 24);
    test:assertEquals(outputs[4], p);
    test:assertEquals(outputs[5], "Santa Clara");
    test:assertEquals(outputs[6], ());
    test:assertEquals(outputs[7], ());
    test:assertEquals(outputs[8], "San Jose");
}
