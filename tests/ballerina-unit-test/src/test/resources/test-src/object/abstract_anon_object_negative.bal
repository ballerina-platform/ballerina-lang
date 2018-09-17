
abstract object {public int age, public string name, new(age, name) {}} p1;
abstract object {public int age, public string name, function test();} p2;
abstract object {public int age, public string name, function test();} p3 = new;

function test() returns int {
    abstract object {public int age, public string name, new(age, name) {}} p4;
    abstract object {public int age, public string name, function test();} p5;
    abstract object {public int age, public string name, function test();} p6 = new;
    return 1;
}
