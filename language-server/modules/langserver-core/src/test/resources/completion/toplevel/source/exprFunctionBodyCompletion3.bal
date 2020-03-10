import ballerina/http;

int globalField1 = 12;
int globalField2 = 12;

function toEmployee(Person p) returns Employee => {
    name: p.
};

type Person record {
    string fname;
    string lname;
    int age;
};

type Employee record {
     *Person;
     string name;
};
