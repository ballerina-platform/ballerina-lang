import ballerina/io;

function testStaticMatchPatternsWithFailStmt() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;

    string | int | boolean a4 = 15;
    string | int | boolean a5 = "fail";
    string | int | boolean a6 = false;

    string | int | boolean a7 = "NothingToMatch";

    string[] result = [fooWithFail(a1), fooWithFail(a2), fooWithFail(a3), fooWithFail(a4), fooWithFail(a5),
    fooWithFail(a6), fooWithFail(a7)];

    return result;
}

function testStaticMatchPatternsWithCheckExpr() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;

    string | int | boolean a4 = 15;
    string | int | boolean a5 = "check";
    string | int | boolean a6 = false;

    string | int | boolean a7 = "NothingToMatch";

    string[] result = [fooWithCheck(a1), fooWithCheck(a2), fooWithCheck(a3), fooWithCheck(a4), fooWithCheck(a5),
    fooWithCheck(a6), fooWithCheck(a7)];

    return result;
}

function fooWithFail(string | int | boolean a) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            return "Value is '15'";
        }
        true => {
            return "Value is 'true'";
        }
        false => {
            return "Value is 'false'";
        }
        "fail" => {
             error err = error("custom error", message = "error value");
             fail err;
        }
    } on fail error e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function fooWithCheck(string | int | boolean a) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            return "Value is '15'";
        }
        true => {
            return "Value is 'true'";
        }
        false => {
            return "Value is 'false'";
        }
        "check" => {
            string str = check getError();
            return str;
        }
    } on fail error e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function getError() returns string|error {
    error err = error("Custom Error");
    return err;
}

function testNestedMatchPatternsWithFail() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";

    string | int | boolean a3 = 15;
    string | int | boolean a4 = "HelloWorld";

    string | int | boolean a5 = "fail";
    string | int | boolean a6 = "fail";

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = false;

    string | int | boolean a9 = 15;
    string | int | boolean a10 = 34;

    string | int | boolean a11 = true;
    string | int | boolean a12 = false;

    string[] result = [barWithFail(a1, a2), barWithFail(a3, a4), barWithFail(a5, a6), barWithFail(a7, a8),
    barWithFail(a9, a10), barWithFail(a11, a12)];

    return result;
}

function barWithFail(string | int | boolean a, string | int | boolean b) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            match b {
                34 => {
                    return "Value is '15 & 34'";
                }
                "HelloWorld" => {
                    return "Value is '15 & HelloWorld'";
                }
            }
        }
        "fail" => {
            match b {
                "fail" => {
                     error err = error("custom error", message = "error value");
                     fail err;
                }
                "HelloWorld" => {
                    return "Value is 'HelloAgain & HelloWorld'";
                }
            }
        }
        true => {
            return "Value is 'true'";
        }
    } on fail error e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function testNestedMatchPatternsWithCheck() returns string[] {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";

    string | int | boolean a3 = 15;
    string | int | boolean a4 = "HelloWorld";

    string | int | boolean a5 = "check";
    string | int | boolean a6 = "check";

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = false;

    string | int | boolean a9 = 15;
    string | int | boolean a10 = 34;

    string | int | boolean a11 = true;
    string | int | boolean a12 = false;

    string[] result = [barWithCheck(a1, a2), barWithCheck(a3, a4), barWithCheck(a5, a6), barWithCheck(a7, a8),
    barWithCheck(a9, a10), barWithCheck(a11, a12)];

    return result;
}

function barWithCheck(string | int | boolean a, string | int | boolean b) returns string {
    match a {
            12 => {
                return "Value is '12'";
            }
            "Hello" => {
                return "Value is 'Hello'";
            }
            15 => {
                match b {
                    34 => {
                        return "Value is '15 & 34'";
                    }
                    "HelloWorld" => {
                        return "Value is '15 & HelloWorld'";
                    }
                }
            }
            "check" => {
                match b {
                    "check" => {
                        io:println("Inside inner chek");
                         string str = check getError();
                         return str;
                    }
                    "HelloWorld" => {
                        return "Value is 'HelloAgain & HelloWorld'";
                    }
                }
            }
            true => {
                return "Value is 'true'";
            }
        } on fail error e {
            io:println("Inside error caught");
            return "Value is 'error'";
        }

        return "Value is 'Default'";
}

