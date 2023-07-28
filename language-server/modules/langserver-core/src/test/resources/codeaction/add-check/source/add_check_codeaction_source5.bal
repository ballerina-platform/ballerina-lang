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

public function getInt() returns int|Error1|Error2|Error3 {
    return 10;
}

public function testFunction1() returns Error1? {
    int value = getInt();
}

public function testFunction2() returns error? {
    int value = getInt();
}

public function testFunction3() returns Error3? {
    int value = getInt();
}

public function testFunction4() {
    int value = getInt();
}
