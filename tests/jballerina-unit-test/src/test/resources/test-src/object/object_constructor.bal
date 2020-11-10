int a = 0;

class Person {

    function init() {
        a += 1;
    }
}

public function testObjectInit() returns int {
    Person p;
    p = new;
    return a;
}
