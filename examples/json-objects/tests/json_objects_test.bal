import ballerina/test;
import ballerina/io;

anydata[] outputs = [];
int counter = 0;

// This is the mock function, which will replace the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach any sa in s {
        anydata value = <anydata> sa;
        outputs[counter] = value.clone();
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();

    json jt0 = { name: "apple", color: "red", price: 100 };
    json jt3 = { fname: "John", lname: "Stallone", "age": 30 };
    json jt4 = { fname: "John", lname: "Stallone", "age": 31 };
    map<json> jt5 = {
         fname: "Peter",
         lname: "Stallone",
         address: {
             line: "20 Palm Grove",
             city: "Colombo 03",
             country: "Sri Lanka"
         }
    };

    test:assertEquals(outputs[0], jt0);
    test:assertEquals(outputs[1], "j is map<json>: ");
    test:assertEquals(outputs[2], true);
    test:assertEquals(outputs[3], jt3);
    test:assertEquals(outputs[4], jt4);
    test:assertEquals(outputs[5], jt5);
    test:assertEquals(outputs[6], "Stallone");
}

