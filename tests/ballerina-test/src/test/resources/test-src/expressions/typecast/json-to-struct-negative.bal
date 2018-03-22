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

function testJsonToStruct() returns (Person) {
    json j = { name:"Child", 
               age:25, 
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:null,
                    info:null,
                    marks:null,
                    a:null,
                    score: 4.57,
                    alive:false
               }, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:[56,79],
               a:"any value",
               score: 5.67,
               alive:true
             };
    Person p = (Person) j;
    return p;
}
