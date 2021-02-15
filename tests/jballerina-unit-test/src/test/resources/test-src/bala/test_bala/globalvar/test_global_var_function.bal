import testorg/foo;

function getGlobalVars() returns [int, string, float, any] {
    return [foo:getGlbVarInt(), foo:getGlbVarString(), foo:getGlbVarFloat(), foo:getGlbVarAny()];
}

function accessGlobalVar() returns int {
    int value;
    value = <int>foo:getGlbVarAny();
    return (foo:getGlbVarInt() + value);
}

function changeGlobalVar(int addVal) returns float {
    foo:setGlbVarFloatChange(77 + <float> addVal);
    float value = foo:getGlbVarFloatChange();
    return value;
}

function getGlobalFloatVar() returns float {
    _ = changeGlobalVar(3);
    return foo:getGlbVarFloatChange();
}

function getGlobalVarFloat1() returns float {
    return foo:getGlbVarFloat1();
}

function initializeGlobalVarSeparately() returns [json, float] {
    foo:setGlbVarJson({"name" : "James", "age": 30});
    foo:setGlbVarFloatLater(3432.3423);
    return [foo:getGlbVarJson(), foo:getGlbVarFloatLater()];
}

function getGlobalVarByte() returns byte {
    return foo:getGlbByte();
}

function getGlobalVarByteArray1() returns byte[] {
    return foo:getGlbByteArray1();
}

function getGlobalVarByteArray2() returns byte[] {
    return foo:getGlbByteArray2();
}

function getGlobalVarByteArray3() returns byte[] {
    return foo:getGlbByteArray3();
}


function getGlobalArrays() returns [int, int, int, int, int, int, int] {
    int[2][3] x = foo:getGlbSealed2DArray();
    int[3][] x1 = foo:getGlbSealed2DArray2();
    return [foo:getGlbArray().length(), foo:getGlbSealedArray().length(), foo:getGlbSealedArray2().length(), x.length(),
                                                                        x[0].length(), x1.length(), x1[0].length()];

}
