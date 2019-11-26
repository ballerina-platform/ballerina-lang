function testBindingPattern() {
    Capital capital = {name: "Sri Jayawardenapura Kotte"};
    Country country = {name: "LK", capital: capital};
    // List Binding Pattern
    [int, string, Country, float...] [iVal, sVal, {name: ctName1, cap: {name: cpName1}}, ...rstVal] = [1, "Hello", country, 1.2, 3.4];
    var capitalName = cpName1;
    int iValRef = iVal;
    float[] rstValRef = rstVal;
    // Map Binding pattern
    Country {name: ctName2, cap: {cpName2}} = country;
    var capital2 = cpName2;
    var country2 = ctName2;
    
    Student student = {name: "", "science": 22};
    Student {name: sName, ...marks} = student;
    map<anydata> marksArr = marks;
    
    int noInitVarDef;
    noInitVarDef = 11;
}

type Country record {
    string name;
    Capital capital;
};

type Capital record {|
    string name;
|};

type Student record {|
    string name;
    int...;
|};

function testVarDef() {
    final var refVar1 = "Hello World";
    var refVar2 = refVar1;
    string refVar3;
    refVar3 = "Ballerina";
    refVar2 = refVar3;
}