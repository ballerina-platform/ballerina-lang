struct Person {
    string name;
    int age;
    Person parent;
    json info;
    map address;
    int[] marks;
    any a;
    float score;
    boolean alive;
}

struct Student {
    string name;
    int age;
}

function testStructToStruct () (Student) {
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

function testComplexMapToJson () (json) {
    map m = {name:"Supun",
                age:25,
                gpa:2.81,
                status:true
            };
    json j2 = <json>m;
    return j2;
}


struct Info {
    blob infoBlob;
}

function testStructWithIncompatibleTypeMapToJson () (json) {
    Info info = {};
    json j;
    error err;
    j, err = <json>info;
    if (err != null) {
        throw err;
    }
    return j;

}
