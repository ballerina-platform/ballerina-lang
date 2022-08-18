function createIntWithError() returns int|error {
    return 10;
}

function testCreateVarWithCheckParentHasNoReturn() {
    createIntWithError();
}

function testCreateVarWithCheckParentHasUnionReturnWithNoErrorMember() returns int|string {
    createIntWithError();
}

function testCreateVarWithCheckParentHasUnionWithErrorMember() returns int|string|error {
    createIntWithError();
}

function testCreateVarWithCheckParentHasErrorReturnType() returns error {
    createIntWithError();
}

function testCreateVarWithCheckParentHasAnotherReturnType() returns string {
    createIntWithError();
}
