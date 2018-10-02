function test1() returns (int){
    function (int, int) returns (int) addFunction = func1;
    return addFunction.call(1, 2);
}

function func1 (int a, int b) returns (int) {
    int c = a + b;
    return c;
}

function test2 () returns (string) {
    function (int , int ) returns (string) sumFunction = function (int a, int b) returns (string) {
                                       int value =  a + b;
                                       return "sum is " + value;
                                   };
    return sumFunction.call(1,2);
}

function test3() returns (int){
    int x = test3Callee(1, func1);
    return x;
}

function test3Callee(int a, function (int x, int y) returns (int) func ) returns (int){
    int x = a + func.call(1, 2);
    return x;
}

function test4() returns (string){
    function (string a, string b) returns (string) foo = test4Callee();
    string v = foo.call("hello ", "world.");
    return v;
}

function test4Callee() returns (function (string a, string b) returns (string)){
   return function (string x, string y) returns (string){
             string z = x + y;
             return z;
          };
}

function test5() returns (string){
    function (string, float) returns (string) bar = test5Callee();
    return "test5 " + bar.call("string", 1.0);
}

function test5Callee() returns (function (string, float) returns (string)){
       return test5Ref;
}

function test5Ref(string a, float b) returns (string){
    string c = a + b;
    return c;
}

function test6() returns (string){
    function (string, function (string, float) returns (string)) returns (string) foo = test6Callee();
    return foo.call("test6 ", test5Ref);
}

function test6Callee() returns (function (string, function (string, float) returns (string)) returns (string)){
       return test6Ref;
}

function test6Ref(string a, function (string, float) returns (string) b) returns (string){
    string c = a + b.call(a , 1.0);
    return c;
}

function testFuncWithArrayParams () returns (int){
    string[] s = ["me", "myself"];
    function (string[])  returns (int) x = funcWithArrayParams;
    return x.call(s);
}

function funcWithArrayParams (string[] a) returns (int) {
    return 0;
}

public function getCount(function (int , int ) returns (int) sumFunction, string first, string last) returns string {
    return first + ": " + sumFunction.call(4, 2) + " " + last;
}

function testFunctionPointerAsFuncParam() returns (int, string) {
    function (int , int ) returns (int) sumFunction = function (int a, int b) returns (int) {
                               int value =  a + b;
                               return value;
                           };

    string s = getCount(sumFunction, "Total", "USD");
    return (sumFunction.call(5, 8), s);
}

function testAnyToFuncPointerConversion_1() returns (int) {
    any anyFunc = function (int a, int b) returns (int) {
                int value =  a + b;
                return value;
            };

    function (int , int ) returns (int) sumFunction = check <function (int , int ) returns (int)> anyFunc;
    return sumFunction.call(3, 2);
}

type Person object {
    private int age;

    new (age) {
    }

    function getAge() returns (int) {
        return age;
    }
};

type Student object {
    private int age;
    private int marks;

    function getAge() returns (int) {
        return age;
    }
};

function testFuncPointerConversion() returns (int) {
    function (Student) returns (int) studentFunc = function (Student s) returns (int) {
                return s.getAge();
            };

    function (Person) returns (int) personFunc = studentFunc;
    Person p = new Person(20);
    return personFunc.call(p);
}

function testAnyToFuncPointerConversion_2() returns (int) {
    any anyFunc = function (Student s) returns (int) {
                        return s.getAge();
                    };

    function (Person) returns (int) personFunc = check <function (Person) returns (int)> anyFunc;
    Person p = new Person(23);
    return personFunc.call(p);
}
