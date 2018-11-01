import testorg/foo version v1;

function testAccessConstantWithoutType() returns string {
    return foo:constName;
}

function testAccessConstantWithType() returns string {
    return foo:constAddress;
}

type AB "A"|"B";

function testAccessFiniteType() returns foo:AB {
    return foo:A;
}

// Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11148
// function testReturnFiniteType() returns AB {
//     return foo:A; // Valid because this is same as `return "A";`
// }
