import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function. 
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
    // Invoke the main function. 
    main();
    string out = "alertRoomAction function invoked for Room : 2 and the action : stop";
    test:assertEquals(lengthof outputs, 1);
    test:assertEquals(outputs[0], out);
}
