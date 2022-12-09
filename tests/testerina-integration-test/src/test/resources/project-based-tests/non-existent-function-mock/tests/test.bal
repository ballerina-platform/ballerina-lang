import ballerina/test;

@test:Mock {
    functionName: "createJdbcClient"
}
function getMockClient() returns string {
    return "client";
}
