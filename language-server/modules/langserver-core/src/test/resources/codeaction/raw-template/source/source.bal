xml moduleVarDeclaration = `Module Var Declaration`;

function testFunctionWithStringReturn() returns string {
    return `Ballerina`;
}

function testFunctionWithUnionReturn() returns xml|int|record{| string name; |} {
    return `Ballerina`;
}

function testFunctionWithNoReturn() {
    return `Ballerina`;
}

function testVariableDeclaration1() {
    string name = `Ballerina`;
}

function testVariableDeclaration2() {
    int i = `123`;
}

function testLetExpressions(string item) {
    string letExpr = let string s = `${item}` in "car, van, bus";
}
