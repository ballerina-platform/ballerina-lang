type Person record {
    string name;
    int age;
    Person? parent;
    json info;
    map address;
    int[] marks;
};


type Student record {
    string name;
    int age;
    map address;
    int[] marks;
};

function testStructToStruct() returns (Person) {
    Student s = { name:"Supun", 
                  age:25, 
                  address:{"city":"Kandy", "country":"SriLanka"}, 
                  marks:[24, 81]
                };
    Person p = s;
    return p;
}

function intToFloatImpCast() {
    float[] numbers;
    int a = 999;
    int b = 95;
    int c = 889;
    numbers = [a, b, c];
    float val1 = 160.0;
    float val2 = <float> 160;
    int d;
    float val3 = d;
}
