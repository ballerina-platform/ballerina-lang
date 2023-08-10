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

public type Error1 distinct error<ErrorDetail1>;

public type Error2 distinct error<ErrorDetail2>;

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

public function testFunction5() returns Error3? {
    MyClient cl = new;
    int value1 = cl.method1();
    int value2 = cl->remoteMethod1();
    int value3 = cl->/resource1;
}

class TestClass {
    function testMethod1() returns Error3? {
        int value = getInt();
    }

    function testMethod2() returns Error1? {
        MyClient cl = new;
        int value = cl.method1();
    }
}

public function testFunction6() {
    var obj1 = client object {
        resource function get testMethod1() returns Error1? {
            int value = getInt();
        }
    };

    var obj2 = service object {
        resource function get testMethod2() returns Error1? {
            int value = getInt();
        }
    };
}

client class MyClient {

    function method1() returns int|Error1|Error2|Error3 {
        return 1;
    }

    remote function remoteMethod1() returns int|Error1|Error2|Error3 {
        return 1;
    }

    resource function get resource1() returns int|Error1|Error2|Error3 {
        return 1;
    }

    resource function get testResource1() returns Error3? {
        int value = getInt();
    }
}
