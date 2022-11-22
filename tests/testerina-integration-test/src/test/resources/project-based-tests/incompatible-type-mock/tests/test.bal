import ballerina/test;

@test:Mock {
    functionName: "createJdbcClient"
}
function getMockClient() returns int {
    return 1;
}
