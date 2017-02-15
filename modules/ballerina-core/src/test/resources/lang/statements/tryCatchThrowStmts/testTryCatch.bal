function testNestedTryCatch(int arg) (boolean, string){
    string a = "0";
    try{
        a = a + "1";
        try {
            a = a + "2";
            if(arg > 10){
                exception e = {};
                a = a + "3";
                throw e;
            }
            a = a + "4";
        } catch (exception e) {
            a = a + "5";
            throw e;
        }
        a = a + "6";
    }catch (exception b){
        a = a + "7";
        return true, a;
    }
    a = a + "8";
    return false, a;
}

function testFunctionThrow (int arg)(boolean, string){
    string a = "0";
    try{
        a = a + "1";
        int b = testThrow(arg);
        a = a + "2";
    }catch (exception b){
        a = a + "3";
        return true, a;
    }
    a = a + "4";
    return false, a;
}

function testThrow(int a)(int) {
    int c = a + 10;
    return testNestedThrow(c);
}

function testNestedThrow(int a)(int){
    exception e  = {};
    throw e;
}

function testValid (){
    // this is valid
    exception e;
    try{
        int a = 10;
    } catch (exception e){
        int b = 10;
    }
}