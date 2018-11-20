
object {public int age; public string name; __init(age, name){}} p1;
object {public int age; public string name; function test();} p2;
object {public int age; public string name; function test();} p3 = new;

function test () returns int {
    object {public int age; public string name; __init(age, name){}} p4;
    object {public int age; public string name; function test();} p5;
    object {public int age; public string name; function test();} p6 = new;
    return 1;
}
