type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

type Student record {
    string name;
    int age;
};

function getCountry() returns Country {
    Country country = {
        name: "USA",
        capital: {
            name: "WDC"
        }
    };
    
    return country;
}

function getStudent() returns Student {
    Student student = {
        name: "Bob",
        age: 20
    };

    return student;
}

function getTupleValue() returns [int, [int, string]] {
    return [1, [2, "Hello World"]];
}

function testAssignmentStatement2() {
    string coName;
    string caName;
    string name;
    int age;
    int tupField1;
    int tupField2;
    string tupField3;
    
    // List Binding Pattern
    [tupField1, [tupField2, tupField3]] = getTupleValue();
    // Error Binding Pattern
    {name: coName, capital: {name: caName}} = getCountry();
    {age, name} = getStudent();
    
    error err = error("errorCode", message = "");
    error eCause = error("errorCode", message = ":D");
    string eMessage = "Sample Message";

    string reason;
    string? msg;
    error? errCause;
    map<anydata|error> rest;

    error(reason, message = msg, cause = errCause, ...rest) = err;
    ErrorTypeDesc1(message = msg, cause = errCause, ...rest) = ErrorTypeDesc1(message = eMessage, cause = eCause);
}

type TestDetail record {|
    string message?;
    error cause?;
    anydata|error...;
|};

public type ErrorTypeDesc1 error<REASON_CONST, TestDetail>;

public const string REASON_CONST = "REASON_CONST_VAL";