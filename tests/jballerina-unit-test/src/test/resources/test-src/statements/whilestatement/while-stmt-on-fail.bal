function testWhileStmtWithFail(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        if(a == 3) {
         error err = error("Custom Error");
         fail err;
        }
    } on fail error e {
        str += "-> error caught. Hence value returning";
        return str;
    }
    str += "-> reached end";
    return str;
}

function testWhileStmtWithCheck(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        if(a == 3) {
            int val = check getError();
            str = str.concat(" Value: ", val.toString());
        }
    } on fail error e {
        str += "-> error caught. Hence value returning";
        return str;
    }
    str += "-> reached end";
    return str;
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

function testNestedWhileStmtWithFail() returns string {
    string result = "";
    error err = error("Custom Error");
    int i = 0;
    while (i == 0) {
        i += 1;
        while (i == 1) {
            i += 1;
            while (i == 2) {
                i += 1;
                while (i == 0) {
                }
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}

function testWhileStmtLoopEndingWithFail(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        error err = error("Custom Error");
        fail err;
    } on fail error e {
        str += "-> error caught. Hence value returning";
    }
    str += "-> reached end";
    return str;
}

function testNestedWhileStmtLoopTerminationWithFail() returns string {
    string result = "";
    error err = error("Custom Error");
    int i = 0;
    while (i >= 0) {
        i += 1;
        while (i >= 1) {
            i += 1;
            while (i >= 2) {
                i += 1;
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}
