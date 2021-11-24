public function main(string... args) {
    int a = true;
    int b = foo();
    Student st = new ("Student 1");
    st.name = 10;
    
    Car car = {
        registrationNo: "AB1234",
        model: "Tesla",
        year: 2020
    };
    
    car.year = 2020.5;
}

function foo() returns float {
    return 1.1;
}

class Student {
    string name;
    
    public function init(string name) {
        self.name = name;
    }
}

type Car record {
    string registrationNo;
    string model;
    int year;
};

function testFunction2(int value1, float value2) returns int {
    int total = value1 + value2;
    return total;
}

function useUnion() {
    boolean|int val = testUnion();
}

function testUnion() returns int|string {
    return "test";
}

function getInt() returns int? {
    return ();
}

function checkNilCast() {
    int? val = getInt();
    () nil = val;
}

xml:Text x = xml `Hello <br /> World`;

function checkCast() {
    int|string a = 10;
    string b = a;
}

function checkCast2() {
    any a = 10;
    string b = a;
}

function checkCast3() {
    any[] anyArr = [1,"hello"];
    int[] intArr = anyArr;
}

function checkCastInNamedArg() {
   string | int arg = 10;
   func1(arg1 = arg);
}

public function func1(int arg1) {

}

public type MyType record {|int field1;int field2;|};

public function func2() {
    record {|int field1;int field2;|} a =  <MyType> {field1: 10,field2: 1.0};
    record {|int field1;int field2;|} b =  {field1: 10,field2: <float> 1.0};
}
