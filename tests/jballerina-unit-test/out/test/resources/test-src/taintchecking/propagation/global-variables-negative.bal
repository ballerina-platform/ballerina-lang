string globalVariable = "";
@tainted string taintedGlobalVariable = "";
final string KK = taintedVal();

map<string> M = {};
R REC = {};

public function main (string... args) {
    normalFunction(args[0]);
    sen(taintedGlobalVariable);
    M["val"] = args[0];
    REC.s = args[0];
    REC.ri.s = args[0];
    REC.ri.m["key"] = args[0];
}

public function normalFunction (string normalInput) {
    anotherNormalFunction(normalInput);
}

public function anotherNormalFunction (string anotherNormalInput) {
    globalVariable = anotherNormalInput;
}

public function sen(@untainted string pa) {

}

function taintedVal() returns @tainted string {
    return "val";
}

type R record {
    string s = "";
    RI ri = {};
};

type RI record {
    string s = "";
    map<string> m = {};
};
