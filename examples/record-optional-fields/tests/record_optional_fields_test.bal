import ballerina/test;

string[] outputs = [];
int counter = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = "";
    foreach var v in s {
        outputs[counter] += v.toString();
    }
    counter += 1;
}

@test:Config {}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Person with the non-defaultable required field set: fname=John lname= gender=male");
    test:assertEquals(outputs[1], "Age before setting: ");
    test:assertEquals(outputs[2], "Age after setting: 25");
    test:assertEquals(outputs[3], "Person with values assigned to required fields: fname=Jane lname=Doe gender=female");
}
