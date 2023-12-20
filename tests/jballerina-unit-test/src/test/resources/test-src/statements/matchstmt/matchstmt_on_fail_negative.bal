type ErrorTypeA distinct error;

const TYPE_A_ERROR_REASON = "TypeA_Error";

type ErrorTypeB distinct error;

const TYPE_B_ERROR_REASON = "TypeB_Error";

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
             fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
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
             fail error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
        }
    } on fail ErrorTypeB e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

function testOnFailErrorType(string | int | boolean a) returns string|error {
   var getTypeAError = function () returns int|ErrorTypeA{
       ErrorTypeA errorA = error ErrorTypeA(TYPE_A_ERROR_REASON, message = "Error Type A");
       return errorA;
   };
   var getTypeBError = function () returns int|ErrorTypeB{
       ErrorTypeB errorB = error ErrorTypeB(TYPE_B_ERROR_REASON, message = "Error Type B");
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
            int _ = check getTypeAError();
        }
        "fail" => {
            int _ = check getTypeBError();
        }
    } on fail ErrorTypeB e {
        return "Value is 'error'";
    }

    return "Value is 'Default'";
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

type SampleComplexErrorData record {|
    int code;
    int[2] pos;
    record {string moreInfo;} infoDetails;
|};

type SampleComplexError error<SampleComplexErrorData>;

function testOnFailWithMultipleErrors() {
    string matchString = "fail";
    match matchString {
        12 => {
            fail error SampleComplexError("Transaction Failure", code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
        "fail" => {
            fail error SampleError("Transaction Failure", code = 50, reason = "deadlock condition");
        }
    } on fail var error(msg) {
    }
}
