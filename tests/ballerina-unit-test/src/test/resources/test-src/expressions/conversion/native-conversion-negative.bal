type Person record {
    string name = "";
    int age = 0;
    Person? parent = ();
    json info = {};
    map address = {};
    int[] marks = [];
    any a = ();
    float score = 0.0;
    boolean alive = false;
    !...
};

type Student record {
    string name = "";
    int age = 0;
    !...
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

type Info record {
    byte[] infoBlob = [];
    !...
};

function testStructWithIncompatibleTypeToJson () returns json {
    Info info = {};
    var j = <json>info;
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}
