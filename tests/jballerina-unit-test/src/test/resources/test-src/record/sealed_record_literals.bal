type Department record {|
    string dptName = "";
    Person[] employees = [];
    Person manager = {};
|};

type Person record {|
    string name = "default first name";
    string lname= "";
    map<any> adrs = {};
    int age = 999;
    Person? child = ();
|};

function testStructLiteral1 () returns Department {
    Department p = {};
    return p;
}

function testStructLiteral2 () returns Person {
    Person p = {};
    return p;
}
