type Department object {
    public {
        string dptName;
        Person[] employees;
    }
    
    new (dptName = "", employees) {
        
    }
};

type Person object {
    public {
        string name = "default first name";
        string lname;
        map adrs;
        int age = 999;
        Family family;
    }
};

type Family object {
    public {
        string spouse;
        int noOfChildren;
        string[] children;
    }
};

function testCreateObject () returns (string, map, int) {
    map address1;
    map address = {"country":"USA", "state":"CA"};
    Person emp = new ();
    emp.name = "Jack";
    emp.adrs = address;
    emp.age = 25;
    return (emp["name"], emp["adrs"], emp["age"]);
}

function testObjectOfObject () returns (string) {

    map address = {"country":"USA", "state":"CA"};
    Person emp1 = new ();
    emp1.name = "Jack";
    emp1.adrs = address;
    emp1.age = 25;
    Person emp2 = new ();
    Person[] emps = [emp1, emp2];
    Department dpt = new (emps);

    string country;
    country = <string>dpt["employees"][0]["adrs"]["country"];
    return country;
}

function testReturnObjectAttributes () returns (string) {
    map address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = new ();
    fmly.children = chldrn;
    Person emp1 = new ();
    emp1.name = "Jack";
    emp1.adrs = address;
    emp1.age = 25;
    emp1.family = fmly;
    Person emp2 = new ();
    Person[] employees = [emp1, emp2];
    Department dpt = new (employees);

    dpt["employees"][0]["family"]["children"][0] = "emily";

    return dpt["employees"][0]["family"]["children"][0];
}

function testExpressionAsIndex () returns (string) {
    Family family = new ();
    family.spouse = "Kate";
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testObjectExpressionAsIndex () returns (string) {
    string country;
    Department dpt = new ([]);
    Family fmly = new ();
    fmly.children = [];
    Person emp2 = new ();
    map address = {"country":"USA", "state":"CA"};
    Person emp1 = new ();
    emp1.name = "Jack";
    emp1.adrs = address;
    emp1.age = 25;
    emp1.family = fmly;

    emp1["adrs"]["street"] = "20";
    emp1["age"] = 0;

    dpt["employees"] = [emp1, emp2];
    dpt["employees"][0]["family"]["children"][0] = "emily";
    dpt["employees"][0]["family"]["noOfChildren"] = 1;

    return dpt["employees"][0]["family"]["children"][dpt["employees"][0]["family"]["noOfChildren"] - 1];
}

function testDefaultVal () returns (string, string, int) {
    Person p = new ();
    return (p["name"], p["lname"], p["age"]);
}

function testNestedFieldDefaultVal () returns (string, string, int) {
    Department dpt = new ([]);
    dpt["employees"] = [];
    dpt["employees"][0]= new Person();
    dpt["employees"][0]["lname"] = "Smith";
    return (dpt["employees"][0]["name"], dpt["employees"][0]["lname"], dpt["employees"][0]["age"]);
}

function testGetNonInitAttribute () returns (string) {
    Person emp1 = new ();
    Person emp2 = new ();
    Person[] emps = [emp1, emp2];
    Department dpt = new (dptName = "HR", emps);
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitArrayAttribute () returns (string) {
    Department dpt = new (dptName = "HR", []);
    return dpt["employees"][0]["family"]["children"][0];
}

function testGetNonInitLastAttribute () returns (Person) {
    Department dpt = new ([]);
    return dpt["employees"][0];
}

function testSetFieldOfNonInitChildObject () {
    Person person = new ();
    person.name = "Jack";
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitObject () {
    Department dpt = new ([]);
    dpt.dptName = "HR";
}
