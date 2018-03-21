import ballerina.io;

struct Person {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
    Family family = {spouse : "Jane"};
    //Person|null parent;
}

struct Family {
    string spouse;
    int noOfChildren;
    string[] children = ["Alex"];
}

function testCreateStruct () {
    Person emp = {name:"Jack"};
    io:println(emp);
    //return (emp.name, emp.adrs, emp.age);
}
