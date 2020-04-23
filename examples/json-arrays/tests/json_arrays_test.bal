import ballerina/test;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    foreach var str in s {
        outputs[counter] = str;
        counter += 1;
    }
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();

    json jt1 = [
        1,
        false,
        null,
        "foo",
        {
            "first": "John",
            "last": "Pala"
        }
    ];

    json jt2 = {"first": "John", "last": "Pala"};
    json jt3 = [1, false, null, "foo", 8.0];
    json jt4 = {
        "fname": "John",
        "lname": "Stallone",
        "family": [
            {
                "fname": "Peter",
                "lname": "Stallone"
            },
            {
                "fname": "Emma",
                "lname": "Stallone"
            },
            {
                "fname": "Alisha",
                "lname": "Stallone"
            },
            {
                "fname": "Paul",
                "lname": "Stallone"
            }
        ]
    };
    json jt5 = { "fname": "Peter", "lname": "Stallone" };
    json jt6 = { "fname": "Emma", "lname": "Stallone" };
    json jt7 = { "fname": "Alisha", "lname": "Stallone" };
    json jt8 = { "fname": "Paul", "lname": "Stallone" };
    string o1 = "length of the array: ";

    test:assertEquals(outputs[0], jt1.toJsonString());
    test:assertEquals(outputs[1], jt2.toJsonString());
    test:assertEquals(outputs[2], jt3.toJsonString());
    test:assertEquals(outputs[3], jt4.toJsonString());
    test:assertEquals(outputs[4], o1);
    test:assertEquals(outputs[5], 4);
    test:assertEquals(outputs[6], jt5.toJsonString());
    test:assertEquals(outputs[7], jt6.toJsonString());
    test:assertEquals(outputs[8], jt7.toJsonString());
    test:assertEquals(outputs[9], jt8.toJsonString());
}
