function testInvalid1 () {
    try {
        int a = 10;
    } catch (error e) {
        error e = {msg:"test"};
        int b = 10;
    }
}

function testInvalid2 () {
    int a;
    throw a;
}

function testInvalid3 () {
    throw funcReturnInt();
}

function funcReturnInt () (int) {
    int a = 10;
    return a;
}

struct TestError {
    string msg;
    error? cause;
    stackFrame[] stackTrace;
    string code;
}

function testInvalid4 () (string) {

    try {
        string a = "abc";
    } catch (TestError e) {
        return "catch1";
    } catch (error ex) {
        return "catch2";
    } catch (TestError e) {
        return "catch3";
    }
    return "done";
}
