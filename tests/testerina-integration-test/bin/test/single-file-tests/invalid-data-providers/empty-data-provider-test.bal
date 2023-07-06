import ballerina/test;

@test:Config {
    dataProvider: provider
}
function test(int i) returns error? {
}

function provider() returns map<[int]> {
    return {};
}

@test:Config {
    dataProvider: provider2
}
function test2(int i) returns error? {
}

function provider2() returns int[]{
    return [];
}
