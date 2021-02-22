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

