function testFunction(boolean paramBool) {
    boolean bool = true;
    while bool {
        if paramBool {
            panic error("panic");
        }
    }

    doSomething(1);
}

function doSomething(int b) {

}
