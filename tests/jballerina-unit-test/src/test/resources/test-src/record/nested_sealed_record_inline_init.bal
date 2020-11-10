final string child = "Bob";

type Person record {|
    string name = "default first name";
    string fname = "";
    string lname = "";
    map<any> adrs = {};
    int age = 999;

    Family family = {spouse : "Jane"};
|};

type Family record {|
    string spouse = "";
    int noOfChildren = 0;
    string[] children = ["Alex", child];
|};

function testCreateStruct () returns Person {
    Person emp = {lname:"Doe"};
    return emp;
}
