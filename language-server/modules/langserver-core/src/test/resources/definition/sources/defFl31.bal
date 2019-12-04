type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

type Student record {
    string name;
    int age;
};

function getCountry() returns Country {
    Country country = {
        name: "USA",
        capital: {
            name: "WDC"
        }
    };
    
    return country;
}

function getStudent() returns Student {
    Student student = {
        name: "Bob",
        age: 20
    };

    return student;
}

function getTupleValue() returns [int, [int, string]] {
    return [1, [2, "Hello World"]];
}

function testAssignmentStatement2() {
    string coName;
    string caName;
    string name;
    int age;
    int tupField1;
    int tupField2;
    string tupField3;
    
    // List Binding Pattern
    [tupField1, [tupField2, tupField3]] = getTupleValue();
    // Error Binding Pattern
    {name: coName, capital: {name: caName}} = getCountry();
    {age, name} = getStudent();
    // Todo: Need to add the Error Binding Pattern
}