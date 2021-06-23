function variableDefaultValue () returns [int, boolean, string, float] {
    int i = 0;
    boolean b = false;
    string s = "";
    float f = 0.0;

    return [i, b, s, f];
}

function inlineVarInit () returns [int, boolean, string, float] {
    int i = 10;
    boolean b = true;
    string s = "hello";
    float f = 2.6;

    return [i, b, s, f];
}


function updateDefaultValue (int v1, boolean v3, string v4, float v5) returns [int, boolean, string, float] {
    int i = 0;
    boolean b = false;
    string s = "";
    float f = 0.0;

    i = v1;
    b = v3;
    s = v4;
    f = v5;

    return [i, b, s, f];
}


function updateVarValue (int v1, boolean v3, string v4, float v5) returns [int, boolean, string, float] {
    int i = 10;
    boolean b = true;
    string s = "hello";
    float f = 2.6;

    i = v1;
    b = v3;
    s = v4;
    f = v5;

    return [i, b, s, f];
}

function wildCardLocalVariables() {
    float _ = 3.14;
    var _ = 10 * 30;
    int x = let var _ = 10 in 10 * 20;
}
