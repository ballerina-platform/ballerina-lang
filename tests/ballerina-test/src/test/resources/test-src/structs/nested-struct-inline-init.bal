import ballerina.io;

struct Person {
    string name = "default first name";
    string fname;
    string lname;
    map adrs;
    int age = 999;
    
    // FIXME: below var is allowed not to be initialize!!!
    Family family = {spouse : "Jane"};
}

struct Family {
    string spouse;
    int noOfChildren;
    string[] children = ["Alex"];
}

function testCreateStruct () {
    Person emp = {lname:"Doe"};
    io:println(emp);
    //return (emp.name, emp.adrs, emp.age);
}
