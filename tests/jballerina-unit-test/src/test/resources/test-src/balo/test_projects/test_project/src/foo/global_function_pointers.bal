function (string a, int b) returns string glf1 = concat;

function (string a, boolean b) returns string glf2 = function (string a, boolean b) returns string {
                                                               return a + b.toString();
                                                           };

function (string, int) returns string glf3 = function (string a, int b) returns string {
    return "llll";
};

public function getGlf1() returns function (string a, int b) returns string {
    return glf1;
}

public function getGlf2() returns function (string a, boolean b) returns string {
    return glf2;
}

public function setGlf2(function (string a, boolean b) returns string func) {
    glf2 = func;
}

public function getGlf3() returns function (string, int) returns string {
    return glf3;
}

public function setGlf3(function (string, int) returns string func) {
    glf3 = func;
}

function concat (string x, int y) returns string {
    string result = x + y.toString();
    return result;
}
