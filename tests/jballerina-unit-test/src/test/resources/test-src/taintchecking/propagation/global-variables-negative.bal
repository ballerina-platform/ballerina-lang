string globalVariable = "";
@tainted string taintedGlobalVariable = "";

public function main (string... args) {
    normalFunction(args[0]);
    sen(taintedGlobalVariable);
}

public function normalFunction (string normalInput) {
    anotherNormalFunction(normalInput);
}

public function anotherNormalFunction (string anotherNormalInput) {
    globalVariable = anotherNormalInput;
}

public function sen(@untainted string pa) {

}