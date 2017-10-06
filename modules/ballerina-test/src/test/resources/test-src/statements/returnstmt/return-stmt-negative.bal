import ballerina.net.http;

service<http> helloWorld {

    resource sayHello(http:Request req, http:Response res) {
        http:Response resp = {};
        return resp;
    }
}

function testNotEnoughArgsToReturn1() (int, int){
    return;
}

function testNotEnoughArgsToReturn2(string s) (string, string){
    return split(s);
}

function testNotEnoughArgsToReturn3(string s) (string, string, int){
    return split(s), "sameera";
}

function testTooManyArgsToReturn1(string s) (string){
    return split(s), "sameera";
}

function testTooManyArgsToReturn2(string s) (string){
    return split2(s);
}

function testInputTypeMismatch1(string s) (string, int, string){
    return split3(s);
}

function testInputTypeMismatch2(string s) (string, int, boolean){
    return split(s), 5, 5;
}

function split(string s) (string) {
    return s;
}

function split2(string s) (string, string) {
    return s, "";
}

function split3(string s) (string, string, string) {
    return s, "", s;
}

function testReturnFromIf() (int) {
    int x = 5;

    if (x > 10) {
        return 500;
    }
}

function testReturnFromIfElse() (int) {
    int x = 5;

    if (x > 10) {
        return 500;
    } else {
        x = 100;
    }
}

function testReturnFromNestedIf() (int) {
    int x = 5;
    int y = 10;

    if (x > 10) {
        if (y < 10) {
            return 0;
        }
    } else {
        return 100;
    }
}

function testReturnFromNestedIf2() (int) {
    int x = 5;
    int y = 10;

    if (x > 10) {
        if (y < 10) {
            y = 0;
        } else {
            return 0;
        }
    } else {
        return 100;
    }
}

function testReturnFromNestedIf3() (int) {
    int x = 5;
    int y = 10;

    if (x > 10) {
        if (y < 10) {
            return -1;
        } else {
            return 0;
        }
    } else {
        if (y > 20) {
            return 30;
        } else {
            y = 50;
        }
    }
}

function returnStmtBranch1(int value, int b) (int) {

    if( value > 10) {
        return 100;

    } else if ( value == 10) {
        return 200;

    } else {

        if (b > 10) {
            return 300;

        } else if ( b == 10){
            return 400;
        }

        return 500;
    }

    return 1;
}

function returnStmtBranch2(int value, int b) (int) {
    if( value > 10) {
        return 100;
    } else if ( value == 10) {
        return 200;
    } else {
        if (b > 10) {
            return 300;
        } else{
            return 400;
        }
        return 500;
    }
}

function returnStmtBranch3 (int value, int b) (int) {
    if (value > 10) {
        return 100;
    } else if (value == 10) {
        return 200;
    } else {
        if (b > 10) {
            return 300;
        } else {
            if (value == 100) {
                return 100;
            } else {
                return -100;
            }
            return 400;
        }
    }
}

function returnStmtBranch4 (int value, int b) (int) {
    if (value > 10) {
        return 100;
    } else if (value == 10) {
        return 200;
    } else {
        if (b > 10) {
            return 300;
        } else {
            if (value == 100) {
                return 100;
            } else {
                return -100;
            }
        }
        return 500;
    }
}

function returnStmtBranch5 (int value, int b) (int) {
    if (value > 10) {
        return 100;
    } else if (value == 10) {
        return 200;
    } else {
        if (b > 10) {
            return 300;
        } else {
            if (value == 100) {
                return 100;
            } else {
                return -100;
            }
        }
    }
    return 500;
}

function testMultiValueInSingleContext(string s) (string, int, string){
    return split4(s), 5, "ss";
}

function split4(string s) (string, int) {
    return s, 4;
}