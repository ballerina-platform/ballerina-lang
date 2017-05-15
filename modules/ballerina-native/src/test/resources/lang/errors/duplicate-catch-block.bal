import ballerina.lang.error;

struct testError {
    string msg;
    string code;
}

function test()(string){

    try {
        string a = "abc";
    } catch (testError e) {
        return "catch1";
    } catch (error:error ex) {
        return "catch2";
    } catch (testError e){
        return "catch3";
    }
    return "done";
}
