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

function testStructToJson() (json) {
    Person p = { name:"Child", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Colombo", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[87,94,72]
               };
    json j = (json) p;
    return j;
}
