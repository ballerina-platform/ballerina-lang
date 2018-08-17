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
    numbers = [999,95,889];
    float val1 = 160.0;
    float val2 = <float> 160;
    float val3 = 160;
}
