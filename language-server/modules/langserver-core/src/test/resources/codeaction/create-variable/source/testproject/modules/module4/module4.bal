import ballerina/module1;

public type ErrorDetail1 record {|
    string cause;
    int code;
|};

public type ErrorDetail2 record {|
    string code;
|};

public type ErrorDetail3 record {|
    string code;
|};

public type Error1 error<ErrorDetail1>;

public type Error2 error<ErrorDetail2>;

public type Error3 distinct Error1;

public function getInt1() returns int|Error1|Error2|Error3 {
    return 10;
}

public function getInt2() returns int|Error2|Error3 {
    return 10;
}

public function getInt3() returns int|module1:ErrorOne|module1:ErrorTwo|Error1 {
    return 10;
}

public function getValueOrError1() returns int|module1:Error1 => 1;

public function getValueOrError2() returns int|module1:Error => 1;

public function getValueOrError3() returns int|module1:Error|module1:Error1 => 1;

public type Error4 distinct error;
type Error5 distinct error;
public type NonPublicError int|Error5;

public function getValueOrError4() returns NonPublicError|Error4 => 1;
