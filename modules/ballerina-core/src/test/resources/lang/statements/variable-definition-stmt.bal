function variableDefaultValue () (int, long, boolean, string, float, double) {
    int i;
    long l;
    boolean b;
    string s;
    float f;
    double d;

    return i, l, b, s, f, d;
}

function inlineVarInit () (int, long, boolean, string, float, double) {
    int i = 10;
    long l = 5l;
    boolean b = true;
    string s = "hello";
    float f = 2.6;
    double d = 3.14159265359d;

    return i, l, b, s, f, d;
}


function updateDefaultValue (int v1, long v2, boolean v3, string v4, float v5, double v6) (int, long, boolean, string, float, double) {
    int i;
    long l;
    boolean b;
    string s;
    float f;
    double d;

    i = v1;
    l = v2;
    b = v3;
    s = v4;
    f = v5;
    d = v6;

    return i, l, b, s, f, d;
}


function updateVarValue (int v1, long v2, boolean v3, string v4, float v5, double v6) (int, long, boolean, string, float, double) {
    int i = 10;
    long l = 5l;
    boolean b = true;
    string s = "hello";
    float f = 2.6;
    double d = 3.14159265359d;

    i = v1;
    l = v2;
    b = v3;
    s = v4;
    f = v5;
    d = v6;

    return i, l, b, s, f, d;
}