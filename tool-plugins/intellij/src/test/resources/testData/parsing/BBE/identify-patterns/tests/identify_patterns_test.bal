import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function. 
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
    // Invoke the main function. 
    main();
    string out1 = "alertRoomAction function invoked for Room : 2 and the action : stop";
    string out2 = "alertRoomAction function invoked for Room : 2 and the action : stop";
    test:assertEquals(out1, outputs[0]);
    test:assertEquals(out1, outputs[1]);
}
