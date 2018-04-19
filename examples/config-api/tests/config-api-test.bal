import ballerina/config;
import ballerina/test;
import ballerina/io;

any [] outputs = [];
int counter = 0;
 // This is the mock function which will replace the real function
@test:Mock {
    packageName : "ballerina.io" ,
    functionName : "println"
}
public function mockPrint (any s) {
    outputs[counter] = s;
    counter++;
}

// Before function to set the config values
function setConfigValues() {
    config:setConfig("username.instances","john,peter");
    config:setConfig("john.access.rights", "RW");
    config:setConfig("peter.access.rights", "R");
    config:setConfig("sum.limit", "5");
}

@test:Config {
    before: "setConfigValues"
}
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals("john has RW access", outputs[0]);
    test:assertEquals("peter has R access", outputs[1]);
    test:assertEquals("Before changing sum.limit in code: 5", outputs[2]);
    test:assertEquals("After changing sum.limit: 10", outputs[3]);
}
