
function test () returns int {
    Person p = new();
    return 6;
}

type Person object {
    public {
        int age,
    }

    function test0(int age, string name) returns string; //param type mismatch

    function test1(int age, string name) returns string; //param name mismatch

    function test2(int age, string name = "hello") returns string; //default param name mismatch

    function test3(int age, string name = "hello") returns string; //default param type mismatch

    //function test4(int age, string name = "hello") returns string; //default param value mismatch

    function test5(int age, string name) returns string; //return param mismatch

    function test6(int age, string name) returns string; //public in attache function

    public function test7(int age, string name) returns string; //public in interface function

    function test8(int age, string name) returns (int | string); //return mismatch

    function test9(int age, string name) returns string; // return missing

    function test10(int age, string name) returns (int, string); // return mismatch

    function test11(int age, string name) returns Foo; //return assignable object object mismatch

    function test12(int age, string name, string... val) returns string; //var args name mismatch

    function test13(int age, string name, string... val) returns string; //var args type mismatch

};

function Person::test0(string age, string name) returns string {
    return "hello";
}

function Person::test1(int agea, string name) returns string {
    return "hello";
}

function Person::test2(int age, string namea = "hello") returns string {
    return "lll";
}

function Person::test3(int age, int name = 5) returns string {
    return "ppp";
}

//function Person::test4(int age, string name = "helloa") returns string {
//    return "kkk";
//}

function Person::test5(int age, string name) returns int {
    return 7;
}

public function Person::test6(int age, string name) returns string {
    return "ddd";
}

function Person::test7(int age, string name) returns string{
    return "ppp";
}

function Person::test8(int age, string name) returns (string | int) {
    return 8;
}

function Person::test9(int age, string name) {

}

function Person::test10(int age, string name) returns (string, string) {
    return ("a", "b");
}

function Person::test11(int age, string name) returns Bar {
    return new Bar();
}

function Person::test12(int age, string name, string... vala) returns string {
    return "uuu";
}

function Person::test13(int age, string name, int... val) returns string {
    return "ooo";
}

type Foo object {
    public {
        int age,
    }
};

type Bar object {
    public {
        int age,
    }
};