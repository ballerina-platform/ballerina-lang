
function funcReturnNilImplicit() {
    int a = 10;
    string s = "test";
    if( a > 20 ) {
        return ();
    }

    a = 15;
}

function funcReturnNilExplicit() returns () {
    int a = 11;
    string s = "test";
    if( a > 20 ) {
        return ;
    }

    return ();
}

function funcReturnNilOrError(int a) returns () | error {
    string s = "test";
    if( a > 20 ) {
        error e = error("dummy error message");
        return e;
    }

    return;
}

function funcReturnOptionallyError(int a) returns error? {
    string s = "test";
    if( a > 20 ) {
        error e = error("dummy error message");
        return e;
    }

    return;
}

function testNilReturnAssignment() {
    error? ret = funcReturnNilImplicit();

    () ret1 = funcReturnNilExplicit();

    any a = funcReturnNilExplicit();

    () ret2 = funcAcceptsNil(funcReturnNilExplicit());

    funcReturnNilExplicit();
}

function funcAcceptsNil(() param) {
    any a = param;
    () ret = param;
    json j = param;
    return ret;
}

