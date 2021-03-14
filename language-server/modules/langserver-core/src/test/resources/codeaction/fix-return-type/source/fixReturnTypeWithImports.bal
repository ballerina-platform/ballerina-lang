import ballerina/module1;

function withImports() {
    module1:TestRecord1 rec1 = {};
    return rec1;
}

function withAmbiguousTypes() {
    return {key: "value"};
}
