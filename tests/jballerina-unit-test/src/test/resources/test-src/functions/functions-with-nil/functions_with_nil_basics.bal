
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

function funcReturnNilOrError(int a) returns ()|error {
    var f = function(int a1) returns ()|error {
                string s = "test";
                if (a1 > 20) {
                    error e = error("dummy error message");
                    return e;
                }

                return;
            };

    error|() e = f(a);

    if (a > 20) {
        if (e is error && e.message() == "dummy error message") {
            return;
        }
        panic error("Expected error with message = `dummy error message`, found: " + (typeof e).toString());
    } else {
        if (e is ()) {
            return;
        }
        panic error("Expected nil, found: " + (typeof e).toString());
    }
}

function funcReturnOptionallyError(int a) returns error? {
    var f = function(int a1) returns error? {
                string s = "test";
                if (a1 > 20) {
                    error e = error("dummy error message");
                    return e;
                }

                return;
            };

    error? e = f(a);

    if (a > 20) {
        if (e is error && e.message() == "dummy error message") {
            return;
        }
        panic error("Expected error with message = `dummy error message`, found: " + (typeof e).toString());
    } else {
        if (e is ()) {
            return;
        }
        panic error("Expected nil, found: " + (typeof e).toString());
    }
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

