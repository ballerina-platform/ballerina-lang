import testorg/foo version v1;

type Ballerina "Ballerina";

function testAccessConstantWithoutType() returns Ballerina {
    return foo:constName;
}

type Colombo "Colombo";

function testAccessConstantWithType() returns Colombo {
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

function testAccessTypeWithContInDef() returns foo:CD {
    foo:CD c = "C";
    return c;
}
