
object {public int age; public string name; function init(int age, string name) {}} p1 = new;
object {public int age; public string name; function test();} p2 = new;

function test() returns int {
    object {public int age; public string name; function init(int age, string name) {}} p4;
    object {public int age; public string name; function test();} p5;
    object {public int age; public string name; function test();} p6 = new;
    return 1;
}
