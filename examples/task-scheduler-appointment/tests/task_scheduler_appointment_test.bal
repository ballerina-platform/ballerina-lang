import ballerina/io;
import ballerina/test;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "Schedule is due - Reminder: 1");
    test:assertEquals(outputs[1], "Schedule is due - Reminder: 2");
    test:assertEquals(outputs[2], "Schedule is due - Reminder: 3");
    test:assertEquals(outputs[3], "Schedule is due - Reminder: 4");
    test:assertEquals(outputs[4], "Schedule is due - Reminder: 5");
    test:assertEquals(outputs[5], "Appointment cancelled.");
}
