
function test () returns int {
    Person p = new();
    return 6;
}

class Person {

    public int age = 0;

    //return param mismatch
    function test1(int a, string name) returns string {
        return 5;
    }

    // return missing: moved to object_attached_func_def_negative
    // function test2(int a, string name) returns string {
    //
    // }

    // return mismatch
    function test3(int a, string name) returns [int, string] {
        return ["a", "b"];
    }
}

class Foo {
    public int age = 0;
}

class Bar {
    public int age = 0;
}
