public function main() {
    int myVal = 100;
    myVal += myVal + 12;
    var y = testFunction();
}

function testFunction() returns int {
    var errVar;
    do {
        
    } on fail var varName {
        errVar = varName;
    }
    
    return 0;
}

type CyclicUnion int|CyclicUnion[];

function UseCyclicUnion() {
    CyclicUnion x = 1;
}

enum Colour {
    RED, GREEN, BLUE
}

function testEnumRename() returns Colour {
    Colour clr = RED;
    return RED;
}

function testFunction(int param1) {
    if param1 < 0 {
        //doSomething
    }
}
