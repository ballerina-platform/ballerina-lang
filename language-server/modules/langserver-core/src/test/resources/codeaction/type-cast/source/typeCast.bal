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
    
    car.year = "2020";
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
