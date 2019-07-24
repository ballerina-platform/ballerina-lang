string globalVariable = "";
@tainted string taintedGlobalVariable = "";
final string KK = taintedVal();

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

function taintedVal() returns @tainted string {
    return "val";
}
