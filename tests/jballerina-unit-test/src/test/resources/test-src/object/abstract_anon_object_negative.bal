
abstract object {public int age; public string name; function __init(int age, string name) {}} p1 = new;
abstract object {public int age; public string name; function test();} p2 = new;

function test() returns int {
    abstract object {public int age; public string name; function __init(int age, string name) {}} p4;
    abstract object {public int age; public string name; function test();} p5;
    abstract object {public int age; public string name; function test();} p6 = new;
    return 1;
}
