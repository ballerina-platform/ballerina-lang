
int glbVarInt = 22343;
string glbVarString = "stringval";
float glbVarFloat = 6342.234234;
any glbVarAny = 572343;

float glbVarFloatChange = 23424.0;

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

public function getGlbVarFloatChange() returns float {
    return glbVarFloatChange;
}

public function setGlbVarFloatChange(float value) {
    glbVarFloatChange = value;
}
