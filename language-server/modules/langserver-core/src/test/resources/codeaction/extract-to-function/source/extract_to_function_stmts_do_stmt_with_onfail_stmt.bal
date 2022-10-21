function testFunction(int param) {
    int localVar = 0;
    do {
        if param == 0 {
            fail error("cannot divide by zero");
        }
        localVar /= param;
    } on fail var e {
    	doSomething(e.message());
    }
}

function doSomething(string a) {

}
