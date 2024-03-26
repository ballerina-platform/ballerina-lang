import testproject.module4;

public function getInt() returns int|MyError {
    return 10;
}

type MyError error;

public function testFunction1(string name) {
    getInt();
}

public function testFunction2() returns error? {
    module4:getInt1();
}

public function testFunction3() {
    module4:getInt1();
}

public function testFunction4() returns module4:Error1? {
    module4:getInt2();
}

public function testFunction5() returns module4:Error3? {
    module4:getInt1();
}

public function testFunction6() {
    module4:getInt3();
}

public function testFunction7() returns error? {
    module4:getInt3();
}

function foo1() {
    module4:getValueOrError1();
}

function foo2() {
    module4:getValueOrError2();
}

function foo3() {
    module4:getValueOrError3();
}

function foo4() {
    module4:getValueOrError4();
}
