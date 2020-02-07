string globalVariable = "";
@tainted string taintedGlobalVariable = "";

public function main (string... args) {
    normalFunction("Hello, World!");
    assignToTaintedGlobalVar(args[0]);
}

public function normalFunction (string normalInput) {
    anotherNormalFunction(normalInput);
}

public function anotherNormalFunction (string anotherNormalInput) {
    globalVariable = anotherNormalInput;
}

public function assignToTaintedGlobalVar(string val) {
    taintedGlobalVariable = val;
}
