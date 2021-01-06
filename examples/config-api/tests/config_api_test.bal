import ballerina/test;
import ballerina/config;

string[] outputs = [];

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public isolated function mockPrint(any|error... val) {
    outputs.push(val.reduce(function (any|error a, any|error b) returns string => a.toString() + b.toString(), "").toString());
}

@test:Config { }
function testFunc() {
    // Invoking the main function
    config:setConfig("app.auth.username", "jack");
    config:setConfig("app.auth.password", "pass1");
    main();
    test:assertEquals(outputs[0], "Authenticating user 'jack'");
}
