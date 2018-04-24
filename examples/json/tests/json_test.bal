import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that will replace the real function.
@test:Mock {
    packageName: "ballerina.io",
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

    json jt1 = "Apple";
    json jt2 = 5.36;
    json jt3 = true;
    json jt4 = false;
    json jt5 = {"name": "apple", "color": "red", "price": 5.36};
    json jt6 = [1, false, null, "foo", {"first": "John", "last": "Pala"}];
    test:assertEquals(jt1, outputs[0]);
    test:assertEquals(jt2, outputs[1]);
    test:assertEquals(jt3, outputs[2]);
    test:assertEquals(jt4, outputs[3]);
    test:assertEquals(jt5, outputs[4]);
    //test:assertEquals(jt6, outputs[5]);
}
