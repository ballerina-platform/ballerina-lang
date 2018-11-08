type Department record {
    string dptName;
    Person[] employees;
};

type Person record {
    string name = "default first name";
    string lname;
    map adrs;
    int age = 999;
    Family family;
    Person? parent;
};

type Family record {
    string spouse;
    int noOfChildren;
    string[] children;
};

type Employee record {
    string name = "default first name";
    string lname;
    map address;
    int age = 999;
    Family family;
    Person? parent;
    string designation;
};

function testStructOfStruct () returns string {

    map address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};

    string country;
    country = <string> dpt.employees[0].adrs["country"];
    return country;
}

function testReturnStructAttributes () returns string {
    map address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = {children:chldrn};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2 = {};
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};

    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
}

function testExpressionAsIndex () returns string {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns string {
    string country;
    Department dpt = {};
    Family fmly = {};
    fmly.children = [];
    Person emp2 = {};
    map address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1.adrs["street"] = "20";
    emp1.age = 0;

    dpt.employees = [emp1, emp2];
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}

function testDefaultVal () returns (string, string, int) {
    Person p = {};
    return (p.name, p.lname, p.age);
}

function testNestedFieldDefaultVal () returns (string, string, int) {
    Department dpt = {};
    dpt.employees = [];
    dpt.employees[0] = {lname:"Smith"};
    return (dpt.employees[0].name, dpt.employees[0].lname, dpt.employees[0].age);
}

function testNestedStructInit () returns Person {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", age:50}};
    return p1;
}

type NegativeValTest record {
    int negativeInt = -9;
    int negativeSpaceInt = -8;
    float negativeFloat = -88.234;
    float negativeSpaceFloat = -24.99;
};

function getStructNegativeValues () returns (int, int, float, float) {
    NegativeValTest tmp = {};
    return (tmp.negativeInt, tmp.negativeSpaceInt, tmp.negativeFloat, tmp.negativeSpaceFloat);
}

function getStruct () returns (Person) {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", lname:"ccc", age:50}};
    return p1;
}

function testGetNonInitAttribute () returns string {
    Person emp1 = {};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName:"HR", employees:emps};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitArrayAttribute () returns string {
    Department dpt = {dptName:"HR"};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitLastAttribute () returns Person {
    Department dpt = {};
    return dpt.employees[0];
}

function testSetFieldOfNonInitChildStruct () {
    Person person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    Department dpt = {};
    dpt.dptName = "HR";
}

function testAdditionOfARestField() returns Person {
    Person p = {name:"Foo", mname:"Bar", age:25, height: 5.9};
    p.firstName = "John";
    return p;
}

function testAnydataRestFieldRHSAccess() returns anydata {
    Person p = {};
    anydata name = p.firstName;
    return name;
}

function testAnydataRestFieldRHSIndexAccess() returns anydata {
    Person p = {};
    anydata name = p["firstName"];
    return name;
}

type Person2 record {
    string name;
    int age;
    string...
};

function testStringRestField() returns Person2 {
    Person2 p = {name:"Foo", age:25, lname: "Bar", address:"Colombo"};
    return p;
}

function testStringRestFieldRHSAccess() returns string {
    Person2 p = {};
    string name = p.firstName;
    return name;
}

function testStringRestFieldRHSIndexAccess() returns (string?, string?) {
    Person2 p = {};
    string? name = p["name"];
    string? firstName = p["firstName"];
    return (name, firstName);
}

type Person3 record {
    string name;
    int age;
    int...
};

function testIntRestField() returns Person3 {
    Person3 p = {name:"Foo", age:25, year: 3};
    return p;
}

function testIntRestFieldRHSAccess() returns int {
    Person3 p = {};
    int birthYear = p.birthYear;
    return birthYear;
}

function testIntRestFieldRHSIndexAccess() returns (int?, int?) {
    Person3 p = {};
    int? age = p["age"];
    int? birthYear = p["birthYear"];
    return (age, birthYear);
}

type Person4 record {
    string name;
    int age;
    float...
};

function testFloatRestField() returns Person4 {
    Person4 p = {name:"Foo", age:25, height: 5.9};
    return p;
}

function testFloatRestFieldRHSAccess() returns float {
    Person4 p = {};
    float height = p.height;
    return height;
}

function testFloatRestFieldRHSIndexAccess() returns (float?, float?) {
    Person4 p = {weight: 61.5};
    float? height = p["height"];
    return (p["weight"], height);
}

type Person5 record {
    string name;
    int age;
    boolean...
};

function testBooleanRestField() returns Person5 {
    Person5 p = {name:"Foo", age:25, isEmployed: true};
    return p;
}

function testBooleanRestFieldRHSAccess() returns boolean {
    Person5 p = {};
    boolean isEmployed = p.isEmployed;
    return isEmployed;
}

function testBooleanRestFieldRHSIndexAccess() returns (boolean?, boolean?) {
    Person5 p = {isStudent: true};
    boolean? isEmployed = p["isEmployed"];
    return (p["isStudent"], isEmployed);
}

type Person6 record {
    string name;
    int age;
    map...
};

function testMapRestField() returns Person6 {
    Person6 p = {name:"Foo", age:25, misc:{lname:"Bar", height:5.9, isEmployed:true}};
    return p;
}

function testMapRestFieldRHSAccess() returns map {
    Person6 p = {};
    map misc = p.misc;
    return misc;
}

function testMapRestFieldRHSIndexAccess() returns (map?, map?) {
    map misc;
    Person6 p = {misc:misc};
    map? invMap = p["invMap"];
    return (p["misc"], invMap);
}

type Person7 record {
    string name;
    int age;
    (float|string|boolean)...
};

function testUnionRestField() returns Person7 {
    Person7 p = {name:"Foo", age:25, lname:"Bar", height:5.9, isEmployed:true};
    return p;
}

function testUnionRestFieldRHSAccess() returns float|string|boolean {
    Person7 p = {};
    float|string|boolean miscFields = p.misc;
    return miscFields;
}

function testUnionRestFieldRHSIndexAccess() returns ((float|string|boolean)?, (float|string|boolean)?) {
    Person7 p = {miscField: "Foo"};
    float|string|boolean|() invField = p["invField"];
    return (p["miscField"], invField);
}

type Person8 record {
    string name;
    int age;
    ()...
};

function testNilRestField() returns Person8 {
    Person8 p = {name:"Foo", age:25, lname:()};
    return p;
}

type Person9 record {
    string name;
    int age;
    Department...
};

function testRecordRestField() returns Person9 {
    Person9 p = {name:"Foo", age:25, dpt:{dptName:"Engineering", employees:[]}};
    return p;
}

function testRecordRestFieldRHSAccess() returns Department {
    Person9 p = {};
    Department dept = p.department;
    return dept;
}

function testRecordRestFieldRHSIndexAccess() returns (Department?, Department?) {
    Person9 p = {dept:{}};
    Department? dept = p["department"];
    return (p["dept"], dept);
}

type Animal object {
    public string kind;
    public string name;

    new(name, kind){
    }
};

type Person10 record {
    string name;
    int age;
    Animal...
};

function testObjectRestField() returns Person10 {
    Person10 p = {name:"Foo", age:25, pet:new Animal("Miaw", "Cat")};
    return p;
}

function testObjectRestFieldRHSAccess() returns Animal {
    Person10 p = {};
    Animal pet = p.pet;
    return pet;
}

function testObjectRestFieldRHSIndexAccess() returns (Animal?, Animal?) {
    Animal anim = new("Rocky", "Dog");
    Person10 p = {pet:anim};
    Animal? pet = p["invPet"];
    return (p["pet"], pet);
}

type Person11 record {
    string name;
    int age;
    (float, string, Animal)...
};

function testTupleRestField() returns Person11 {
    Person11 p = {name:"Foo", age:25, misc:(5.9, "Bar", new Animal("Miaw", "Cat"))};
    return p;
}

function testTupleRestFieldRHSAccess() returns (float, string, Animal) {
    Person11 p = {};
    (float, string, Animal) tupType = p.tupType;
    return tupType;
}

function testTupleRestFieldRHSIndexAccess() returns ((float, string, Animal)?, (float, string, Animal)?) {
    Person11 p = {tup:(4.5, "foo", new Animal("Miaw", "Cat"))};
    (float, string, Animal)? tupType = p["invTup"];
    return (p["tup"], tupType);
}

type Person12 record {
    string name;
    int age;
    any...
};

function testAnyRestField() returns Person12 {
    Animal[] pets = [new Animal("Miaw", "Cat"), new Animal("Woof", "Dog")];
    Person12 p = {name:"Foo", age:25, pets:pets};
    return p;
}

function testAnyRestFieldRHSAccess() returns any {
    Person12 p = {};
    any a = p.anyField;
    return a;
}

function testAnyRestFieldRHSIndexAccess() returns (any, any) {
    Animal[] pets = [new Animal("Miaw", "Cat"), new Animal("Woof", "Dog")];
    Person12 p = {pets:pets};
    any a = p["anyField"];
    return (p["pets"], a);
}

type PersonA record {
    string fname;
    string lname;
    function() returns string fullName?;
};

function testFuncPtrAsRecordField() returns string {
    PersonA p = {fname:"John", lname:"Doe"};
    p.fullName = function () returns string {
        return p.lname + ", " + p.fname;
    };

    return p.fullName();
}
