function testFunction(int param) {
    int localVar = 0;
    match param {
        1 => {localVar = 1;}
        2 => {doSomething(2);}
    }
    
    doSomething(localVar);
}

function doSomething(int a) {
    
}
