string globalVariable = "";

function main (string... args) {
    normalFunction("Hello, World!");
}

public function normalFunction (string normalInput) {
    anotherNormalFunction(normalInput);
}

public function anotherNormalFunction (string anotherNormalInput) {
    globalVariable = anotherNormalInput;
}
