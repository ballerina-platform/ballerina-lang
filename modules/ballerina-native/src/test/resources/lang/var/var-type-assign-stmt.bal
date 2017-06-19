import ballerina.lang.errors;

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

function testIncompatibleJsonToStructWithErrors() (Person, errors:TypeConversionError) {
    json j = { name:"Child",
               age:25,
               parent:{
                    name:"Parent",
                    age:50,
                    parent: "Parent",
                    address:{"city":"Colombo", "country":"SriLanka"},
                    info:null,
                    marks:null
               },
               address:{"city":"Colombo", "country":"SriLanka"},
               info:{status:"single"},
               marks:[87,94,72]
             };
    var p, err = <Person> j;
    return p, err;
}


struct PersonA {
    string name;
    int age;
}

function testJsonToStructWithErrors() (PersonA, errors:TypeConversionError) {
    json j = {name:"supun"};

    var person, err = <PersonA> j;

    return person, err;
}