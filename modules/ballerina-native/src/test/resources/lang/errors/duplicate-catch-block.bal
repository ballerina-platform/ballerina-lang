import ballerina.lang.errors;

struct TestError {
    string msg;
    errors:Error cause;
    string code;
}

function test()(string){

    try {
        string a = "abc";
    } catch (TestError e) {
        return "catch1";
    } catch (errors:Error ex) {
        return "catch2";
    } catch (TestError e){
        return "catch3";
    }
    return "done";
}
