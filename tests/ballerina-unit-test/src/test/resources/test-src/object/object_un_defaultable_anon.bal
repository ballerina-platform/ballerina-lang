
object {public int age = 0; public string name = ""; function __init(int age, string name){}} p1 = new(1, "Anne");
object {public int age = 0; public string name = ""; function test();} p2 = new;

function test () returns int {
    object {public int age = 0; public string name = ""; function __init(int age, string name){}} p3;
    object {public int age = 0; public string name = ""; function test();} p4;
    object {public int age = 0; public string name = ""; function test();} p5 = new;
    return 1;
}
