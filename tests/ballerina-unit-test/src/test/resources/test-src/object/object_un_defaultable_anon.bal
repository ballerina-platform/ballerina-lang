
object {public int age = 0; public string name = ""; function __init(int age, string name){}} p1;
object {public int age = 0; public string name = ""; function test();} p2;
object {public int age = 0; public string name = ""; function test();} p3 = new;

function test () returns int {
    object {public int age = 0; public string name = ""; function __init(int age, string name){}} p4;
    object {public int age = 0; public string name = ""; function test();} p5;
    object {public int age = 0; public string name = ""; function test();} p6 = new;
    return 1;
}
