type Person {
    string name;
    int age;
    Person parent;
    json info;
    map address;
    int[] marks;
    any a;
    float score;
    boolean alive;
};

type Student {
    string name;
    int age;
};

function testStructToStruct () returns (Student) {
    Person p = {name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    Student s = <Student>p;
    return s;
}

type Info {
    blob infoBlob;
};

function testStructWithIncompatibleTypeToJson () returns (json) {
    Info info = {};
    json j;
    j = check <json>info;
    return j;
}
