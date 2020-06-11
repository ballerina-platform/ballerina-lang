int a = 0;

type Person object {

    function init() {
        a += 1;
    }
};

public function testObjectInit() returns int {
    Person p;
    p = new;
    return a;
}
