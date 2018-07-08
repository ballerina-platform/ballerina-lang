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

    test:assertEquals(jt1, outputs[0]);
    test:assertEquals(jt2, outputs[1]);
    test:assertEquals(jt3, outputs[2]);
    test:assertEquals(jt4, outputs[3]);
}
