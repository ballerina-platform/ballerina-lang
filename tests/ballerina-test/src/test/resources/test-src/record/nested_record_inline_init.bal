string child = "Bob";

type Person {
    string name = "default first name";
    string fname;
    string lname;
    map adrs;
    int age = 999;

    Family family = {spouse : "Jane"};
};

type Family {
    string spouse;
    int noOfChildren;
    string[] children = ["Alex", child];
};

function testCreateStruct () returns Person {
    Person emp = {lname:"Doe"};
    return emp;
}
