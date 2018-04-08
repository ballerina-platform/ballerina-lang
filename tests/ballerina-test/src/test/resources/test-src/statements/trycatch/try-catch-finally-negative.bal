function testInvalid1 (){
    try{
        int a = 10;
    } catch (error e){
        error e = null;
        int b = 10;
    }
}

function testInvalid2 (){
    int a;
    throw a;
}

function testInvalid3 (){
    throw funcReturnInt();
}

function funcReturnInt() returns (int){
    int a = 10;
    return a;
}

public type TestError {
    string message;
    error[] cause;
    string code;
};

function testInvalid4() returns (string){

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
