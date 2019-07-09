int a = 0;

type Person object {

    function __init() {
        a += 1;
    }
};

public function testObjectInit() returns int {
    Person p;
    p = new;
    return a;
}
