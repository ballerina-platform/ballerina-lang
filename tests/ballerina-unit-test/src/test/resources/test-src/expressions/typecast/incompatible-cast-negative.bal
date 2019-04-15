type Person record {
    string name;
    int age;
    Person? parent;
    json info;
    map<any> address;
    int[] marks;
};


type Student record {
    string name;
    int age;
    map<any> address;
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
    float val2 = 160;
    int d = 0;
    float val3 = d;
}

function testAnyArrayToJson() returns (json|error) {
    any[] a = [8,4,6];
    json value;
    value = check trap <json> a;
    return value;
}
