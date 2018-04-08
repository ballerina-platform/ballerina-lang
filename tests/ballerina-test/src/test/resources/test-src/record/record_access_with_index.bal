type Department {
    string dptName;
    Person[] employees;
};

type Person {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
    Family family;
};

type Family {
    string spouse;
    int noOfChildren;
    string[] children;
};

function testCreateStruct () returns (string, map, int) {
    map address1;
    map address = {"country":"USA", "state":"CA"};
    Person emp = {name:"Jack", adrs:address, age:25};
    return (emp["name"], emp["adrs"], emp["age"]);
}

function testStructOfStruct () returns (string) {

    map address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};

    string country;
    country = <string>dpt["employees"][0]["adrs"]["country"];
    return country;
}

function testReturnStructAttributes () returns (string) {
    map address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = {children:chldrn};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2 = {};
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};

    dpt["employees"][0]["family"]["children"][0] = "emily";

    return dpt["employees"][0]["family"]["children"][0];
}

function testExpressionAsIndex () returns (string) {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns (string) {
    string country;
    Department dpt = {};
    Family fmly = {};
    fmly.children = [];
    Person emp2 = {};
    map address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1["adrs"]["street"] = "20";
    emp1["age"] = 0;

    dpt["employees"] = [emp1, emp2];
    dpt["employees"][0]["family"]["children"][0] = "emily";
    dpt["employees"][0]["family"]["noOfChildren"] = 1;

    return dpt["employees"][0]["family"]["children"][dpt["employees"][0]["family"]["noOfChildren"] - 1];
}

function testDefaultVal () returns (string, string, int) {
    Person p = {};
    return (p["name"], p["lname"], p["age"]);
}

function testNestedFieldDefaultVal () returns (string, string, int) {
    Department dpt = {};
    dpt["employees"] = [];
    dpt["employees"][0] = {lname:"Smith"};
    return (dpt["employees"][0]["name"], dpt["employees"][0]["lname"], dpt["employees"][0]["age"]);
}

function testGetNonInitAttribute () returns (string) {
    Person emp1 = {};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName:"HR", employees:emps};
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitArrayAttribute () returns (string) {
    Department dpt = {dptName:"HR"};
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitLastAttribute () returns (Person) {
    Department dpt = {};
    return dpt["employees"][0];
}

function testSetFieldOfNonInitChildStruct () {
    Person person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    Department dpt = {};
    dpt.dptName = "HR";
}
