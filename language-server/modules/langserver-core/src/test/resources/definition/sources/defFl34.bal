public const int CONST_VAL = 1;

function testMatchStatement1() {
    int refVar = 0;
    match getTupleValue() {
        [1, [1, {name: "A"}]] => {
            refVar = 1;
        }
        // List Binding Pattern
        var [iVal1, [iVal2, studentVal]] => {
            int a = iVal2;
        }
        // Variable name binding pattern
        var varVal => {
            int a = varVal[0];
        }
    }
    match getTupleValue() {
        var [iVal1, [iVal2, {age: a, name: n}]] => {
            string name = n;
        }
        // Wildcard binding pattern
        _ => {
            refVar = 2;
        }
    }
    match getTupleValue() {
        // List Binding Pattern
        var [iVal1, [iVal2, {age, name}]] => {
            string n = name;
        }
    }
    match getTupleValue() {
        // List Binding Pattern
        var [iVal1, [iVal2, {age: a, name: n}]] => {
            string name = n;
        }
    }
    match getIntVal() {
        1 => {
            refVar = 1;
        }
    }
}

function testMatchStatement2() {
    int refVal = 0;
    match getMappingValue() {
        {name: "USA", capital: {name: "WDC", code: 1}} => {
            refVal = 1;
        }
        var {name, capital} => {
            string capName = capital.name;
        }
    }

    match getMappingValue() {
        var {name: coName, capital: {name, code: c}} => {
            int capCode = c;
        }
    }
}

function testConstPattern() {
    int refVal = -1;
    match getIntVal() {
        100 => {
            refVal = 1;
        }
        CONST_VAL => {
            refVal = 2;
        }
    }
}

function getIntVal() returns int {
    return 0;
}

function getTupleValue() returns [int, [int, Student]] {
    Student student = {
        age: 20,
        name: "Bob"
    };
    return [1, [2, student]];
}

function getMappingValue() returns Country {
    Country country = {
        name: "USA",
        capital: {
            name: "WDC",
            code: 111
        }
    };

    return country;
}

type Student record {
    int age;
    string name;
};

type Country record {
    string name;
    Capital capital;
};

type Capital record {
    string name;
    int code;
};

type TestDetail record {|
    string message?;
    error cause?;
    anydata|error...;
|};

public function testFunction(string args) {
    error eCause = error("errorCode", message = ":D");
    string eMessage = "Sample Message";

    error err = error("errorCode", message = "");
    
    match err {
        var ErrorTypeDesc1(message = msg, cause = errCause, ...rest) => {
            var msgVal = msg;
            var causeVal = errCause;
            var restVal = rest;
        }
        var error(reason, message = msg, cause = errCause, ...rest) => {
            var reasonVal = reason;
            var msgVal = msg;
            var causeVal = errCause;
            var restVal = rest;
        }
    }

    match err {
        error(var reason, message = msg, cause = errCause, ...var rest) => {
            var reasonVal = reason;
            var msgVal = msg;
            var causeVal = errCause;
            var restVal = rest;
        }
    }

    match err {
        error("", message = msg, cause = errCause, ...var rest) => {
            var msgVal = msg;
            var causeVal = errCause;
            var restVal = rest;

        }
    }    
}

public type ErrorTypeDesc1 error<REASON_CONST, TestDetail>;

public const string REASON_CONST = "REASON_CONST_VAL";