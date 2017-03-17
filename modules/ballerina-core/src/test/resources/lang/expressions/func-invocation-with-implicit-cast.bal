function testImplicitCastInvocation() (string) {

    int input = 7;
    string output = modifyInt(input);
    return output;
}

function modifyInt(string a)(string) {
    string b = a + ".modified";
    return b;
}





function testExactMatchWhenCastPossible() (double){
    double val = 9;
    return exactMatch(val);
}

function exactMatch(double b) (double) {
    return b * 2;
}

function exactMatch(string b) (double) {
    double a = 80;
    return a;
}





function testImplicitCastInvocationWithMultipleParams() (string) {
    int a = 8;
    float b = 5;
    int c = 4;
    double d = 4;
    int e = 2;
    return multiParam(a, b, c, d, e);
}

function multiParam(string a, double b, float c, string d, int e) (string) {
    string result = a + e;
    return result;
}



