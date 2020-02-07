function testBindingPattern() {
    Capital capital = {name: "Sri Jayawardenapura Kotte"};
    Country country = {name: "LK", capital: capital};
    // List Binding Pattern
    [int, string, Country, float...] [iVal, sVal, {name: ctName1, cap: {name: cpName1}}, ...rstVal] = [1, "Hello", country, 1.2, 3.4];
    var capitalName = cpName1;
    int iValRef = iVal;
    float[] rstValRef = rstVal;
    // Map Binding pattern
    Country {name: ctName2, cap: {cpName2}} = country;
    var capital2 = cpName2;
    var country2 = ctName2;
    
    Student student = {name: "", "science": 22};
    Student {name: sName, ...marks} = student;
    map<anydata> marksArr = marks;
    
    int noInitVarDef;
    noInitVarDef = 11;
}

type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

type Student record {|
    string name;
    int...;
|};

function testVarDef() {
    final var refVar1 = "Hello World";
    var refVar2 = refVar1;
    string refVar3;
    refVar3 = "Ballerina";
    refVar2 = refVar3;
}

type TestDetail record {|
    string message?;
    error cause?;
    anydata|error...;
|};

public function testFunction(string args) {
    error err = error("errorCode", message = "");
    error eCause = error("errorCode", message = "");
    string eMessage = "Sample Message";
    TestDetail td = {};

    var error(reason, message = msg, cause = errCause, ...rest) = err;
    var ErrorTypeDesc1(message = msg2, cause = errCause2, ...rest2) = ErrorTypeDesc1(message = eMessage, cause = eCause);
    ErrorTypeDesc1 error(reason3, message = msg3, cause = errCause3, ...rest3) = ErrorTypeDesc1(message = eMessage, cause = eCause);

    string resonVal = reason;
    string? messageVal = msg;
    map<anydata|error> restVal = rest;

    string? messageVal2 = msg2;
    map<anydata|error> restVal2 = rest2;

    string resonVal3 = reason3;
    string? messageVal3 = msg3;
    map<anydata|error> restVal3 = rest3;
}

public type ErrorTypeDesc1 error<REASON_CONST, TestDetail>;

public const string REASON_CONST = "REASON_CONST_VAL";