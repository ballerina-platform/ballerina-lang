import ballerina/http;

int globalField1 = 12;
int globalField2 = 12;

function toEmployee(Person p) returns Person => 

type Person record {
    string fname;
    string lname;
    int age;
};
