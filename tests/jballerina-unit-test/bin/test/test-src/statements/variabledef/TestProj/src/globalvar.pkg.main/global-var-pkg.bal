
import globalvar.pkg.variable;

float glbVarFloat = <float> variable:getGlbVarAnyInt();

any glbVarAny = getAnyGlobalVar();

int glbVarInt = variable:getIntValue();

function getGlobalVars() returns [int, string, float, any] {
    return [variable:getGlbVarInt(), variable:getGlbVarString(), variable:getGlbVarFloat(), variable:getGlbVarAny()];
}

function changeGlobalVar(int addVal) returns (float) {
    variable:setGlbVarFloatChange(77 + <float> addVal);
    float value = variable:getGlbVarFloatChange();
    return value;
}

function getGlobalFloatVar() returns (float) {
    _ = changeGlobalVar(3);
    return variable:getGlbVarFloatChange();
}

function getAssignedGlobalVarFloat() returns (float) {
    return glbVarFloat;
}

function getAnyGlobalVar() returns (any) {
    float val = 45545.0;
    return val;
}

function getGlobalVarAny() returns (any) {
    return glbVarAny;
}

function getGlobalVarInt() returns (int) {
    return glbVarInt;
}

