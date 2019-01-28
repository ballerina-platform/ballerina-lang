import ballerina/test;
import ballerina/io;

any[] outputs = [];
int count = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[count] = string.convert(s[0]);
    count += 1;
}

@test:Config
function testFunc() {
    main();
    test:assertEquals(outputs[0], "Create CUSTOMER table status: 0");
    test:assertEquals(outputs[1], "Create SALARY table status: 0");
    test:assertEquals(outputs[2], "Inserted count: 1");
    test:assertEquals(outputs[3], "Transaction committed");
    test:assertEquals(outputs[4], "Drop table CUSTOMER status: 0");
    test:assertEquals(outputs[5], "Drop table SALARY status: 0");
}
