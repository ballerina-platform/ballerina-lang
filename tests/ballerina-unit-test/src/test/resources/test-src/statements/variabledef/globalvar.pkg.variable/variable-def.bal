
int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;
int glbVarAnyInt = 88343;

float glbVarFloatChange = 99.0;

public function getIntValue() returns (int) {
    return 8876;
}

public function getGlbVarInt() returns int {
    return glbVarInt;
}

public function getGlbVarString() returns string {
    return glbVarString;
}

public function getGlbVarFloat() returns float {
    return glbVarFloat;
}

public function getGlbVarAny() returns any {
    return glbVarAny;
}

public function getGlbVarAnyInt() returns int {
    return glbVarAnyInt;
}

public function getGlbVarFloatChange() returns float {
    return glbVarFloatChange;
}

public function setGlbVarFloatChange(float value) {
    glbVarFloatChange = value;
}
