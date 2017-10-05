struct TestError {
    string msg;
    error cause;
    stackFrame[] stackTrace;
    string code;
}

function test()(string){

    try {
        string a = "abc";
    } catch (TestError e) {
        return "catch1";
    } catch (error ex) {
        return "catch2";
    } catch (TestError e){
        return "catch3";
    }
    return "done";
}
