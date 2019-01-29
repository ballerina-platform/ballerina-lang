import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
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
    test:assertEquals(outputs.length(), 4);
    test:assertEquals(outputs[0], p);
    test:assertEquals(outputs[1], "Santa Clara");
    test:assertEquals(outputs[2], ());
    test:assertEquals(outputs[3], "San Jose");
}
