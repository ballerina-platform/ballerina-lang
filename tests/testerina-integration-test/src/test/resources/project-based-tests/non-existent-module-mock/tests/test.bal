import ballerina/test;

@test:Mock {
    moduleName: "module1",
    functionName: "createJdbcClient"
}
test:MockFunction intAddMockFn = new();
