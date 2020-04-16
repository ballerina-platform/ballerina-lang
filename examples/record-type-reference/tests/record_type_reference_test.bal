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
    test:assertEquals(outputs[0], "designation=Software Engineer name=John Doe age=25");
    test:assertEquals(outputs[1], "designation=UX Engineer name=Jane Doe age=25");
    test:assertEquals(outputs[2], "team=designation=Software Engineer name=John Doe age=25 designation=UX Engineer name=Jane Doe age=25 company=XYZ Inc. designation=Engineering Manager name=Mark age=35");
}
