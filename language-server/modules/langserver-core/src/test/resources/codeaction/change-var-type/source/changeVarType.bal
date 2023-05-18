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
    
    future<float> fut = start foo(); 
    float fVal = wait fut;
 
    boolean inferredVal = bar();
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

public type TargetType typedesc<int>;
function bar(TargetType t = <>) returns t = external;
