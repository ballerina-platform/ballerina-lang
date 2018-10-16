import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
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

    json jt1 = {
        "title": "The Revenant",
        "year": "2015",
        "released": "08 Jan 2016",
        "writer": {
            "fname": "Michael",
            "lname": "Punke", "age": 30
        }
    };

    json jt2 = "Punke";
    int jt3 = 30;
    json jt4 = {
        "title": "Inception",
        "year": "2010",
        "released": "16 Jul 2010",
        "writer": {
            "fname": "Christopher",
            "lname": "Nolan",
            "age": 30
        }
    };

    test:assertEquals(outputs[0], jt1);
    test:assertEquals(outputs[1], jt2);
    test:assertEquals(outputs[2], jt3);
    test:assertEquals(outputs[3], jt4);
}
