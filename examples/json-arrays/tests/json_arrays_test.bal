import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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

    json jt2 = "John";
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

    string o1 = "length of array: 4";

    // test:assertEquals(jt1, outputs[0]);
    test:assertEquals(jt2, outputs[1]);
    // test:assertEquals(jt3, outputs[2]);
    test:assertEquals(jt4, outputs[3]);
    test:assertEquals(o1, outputs[4]);
    test:assertEquals(jt5, outputs[5]);
    test:assertEquals(jt6, outputs[6]);
    test:assertEquals(jt7, outputs[7]);
    test:assertEquals(jt8, outputs[8]);
}
