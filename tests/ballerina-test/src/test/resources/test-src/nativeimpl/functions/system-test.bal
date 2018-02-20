function testPrintAndPrintlnString(string s1, string s2){
    println(s1);
    print(s2);
    // output is equal to s1\ns2
}

function testPrintAndPrintlnInt(int v1, int v2){
    println(v1);
    print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnFloat(float v1, float v2){
    println(v1);
    print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnBoolean(boolean v1, boolean v2){
    println(v1);
    print(v2);
    // output is equal to v1\nv2
}

function testPrintAndPrintlnConnector() {
    Foo f1 = create Foo();
    Foo f2 = create Foo();
    println(f1);
    print(f2);
}

function testPrintAndPrintlnFunctionPointer() {
    function (int, int) returns (int) addFunction = func1;
    println(addFunction);
    print(addFunction);
}

function testSprintf(string fmtStr, any[] fmtArgs) (string) {
    return sprintf(fmtStr, fmtArgs);
}

function printNewline() {
    print("hello\n");
}

function testSleep(int timeoutv) {
    sleep(timeoutv);
}

function func1 (int a, int b) (int c) {
    c = a + b;
    return;
}

connector Foo() {
    action bar() (int) {
        return 5;
    }
}