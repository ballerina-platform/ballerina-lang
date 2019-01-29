type Person record {
    string name = "";
    int age = 0;
    string...;
};

function invalidRestField() {
    Person p = {name:"John", age:20, height:6, employed:false, city:"Colombo"};
}

type PersonA record {
    string name = "";
    int age = 0;
};

function emptyRecordForAnyRestField() {
    PersonA p = {name:"John", misc:{}};
}

type Pet record {
    Animal lion;
};

type Bar object {
    int a = 0;
};

function testInvalidRestFieldAddition() {
    PersonA p = {};
    p.invField = new Bar();
}
