import ballerina/test;

@test:Mock { functionName: "intAdd" }
test:MockFunction intAddMockFn = new();
