import ballerina/test;

@test:Mock {
    moduleName: "module1",
    functionName: "createJdbcClient"
}
function getMockClient() returns string {
    return "client";
}
