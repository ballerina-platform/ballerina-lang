import testorg/foo version v1;

function getGlobalVars() returns (int, string, float, any) {
    return (foo:glbVarInt, foo:glbVarString, foo:glbVarFloat, foo:glbVarAny);
}

function accessGlobalVar() returns int {
    int value;
    value = check <int>foo:glbVarAny;
    return (foo:glbVarInt + value);
}

function changeGlobalVar(int addVal) returns float {
    foo:glbVarFloatChange = 77 + <float> addVal;
    float value = foo:glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() returns float {
    _ = changeGlobalVar(3);
    return foo:glbVarFloatChange;
}

function getGlobalVarFloat1() returns float {
    return foo:glbVarFloat1;
}

function initializeGlobalVarSeparately() returns (json, float) {
    foo:glbVarJson = {"name" : "James", "age": 30};
    foo:glbVarFloatLater = 3432.3423;
    return (foo:glbVarJson, foo:glbVarFloatLater);
}

function getGlobalVarByte() returns byte {
    return foo:glbByte;
}

function getGlobalVarByteArray1() returns byte[] {
    return foo:glbByteArray1;
}

function getGlobalVarByteArray2() returns byte[] {
    return foo:glbByteArray2;
}

function getGlobalVarByteArray3() returns byte[] {
    return foo:glbByteArray3;
}


function getGlobalArrays() returns (int, int, int, int, int, int, int) {
    int[2][3] x = foo:glbSealed2DArray;
    int[3][] x1 = foo:glbSealed2DArray2;
    return (foo:glbArray.length(), foo:glbSealedArray.length(), foo:glbSealedArray2.length(), x.length(),
                                                                        x[0].length(), x1.length(), x1[0].length());

}
