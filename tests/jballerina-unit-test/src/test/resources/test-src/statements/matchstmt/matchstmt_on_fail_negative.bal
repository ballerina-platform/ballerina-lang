type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

function testUnreachableAfterFail(string | int | boolean a) returns string|error {
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
             return "After failure throw";
        }
    } on fail error e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function testIncompatibleErrorTypeOnFail(string | int | boolean a) returns string|error {
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
             fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
        }
    } on fail ErrorTypeB e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function testUnreachableInOnFail(string | int | boolean a) returns string|error {
    string str = "";
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
             fail ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
        }
    } on fail ErrorTypeB e {
        return "Value is 'error'";
        str += "-> After returning string";
    }

    return "Value is 'Default'";
}

function testOnFailErrorType(string | int | boolean a) returns string|error {
   var getTypeAError = function () returns int|ErrorTypeA{
       ErrorTypeA errorA = ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
       return errorA;
   };
   var getTypeBError = function () returns int|ErrorTypeB{
       ErrorTypeB errorB = ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
       return errorB;
   };
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
            int resA = check getTypeAError();
        }
        "fail" => {
            int resB = check getTypeBError();
        }
    } on fail ErrorTypeB e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}
