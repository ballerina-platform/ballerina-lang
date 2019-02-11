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
    outputs[counter] = string.convert(s[0]) + string.convert(s[1]) + string.convert(s[2])
                    + string.convert(s[3]) + string.convert(s[4]) + string.convert(s[5]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(outputs[0], "Base Salary: 2500 | Annual Increment: 20 | Bonus Rate: 0.02");
    test:assertEquals(outputs[1], "Base Salary: 2500 | Annual Increment: 100 | Bonus Rate: 0.02");
    test:assertEquals(outputs[2], "Base Salary: 2500 | Annual Increment: 20 | Bonus Rate: 0.1");
    test:assertEquals(outputs[3], "Base Salary: 2500 | Annual Increment: 100 | Bonus Rate: 0.1");
    test:assertEquals(outputs[4], "Base Salary: 2500 | Annual Increment: 100 | Bonus Rate: 0.1");
    test:assertEquals(outputs[5], "Base Salary: 2500 | Annual Increment: 100 | Bonus Rate: 0.1");
}
