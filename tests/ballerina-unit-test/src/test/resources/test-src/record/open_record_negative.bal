type Person record {
    string name,
    int age,
    string...
};

function invalidRestField() {
    Person p = {name:"John", age:20, height:6, employed:false, city:"Colombo"};
}

type PersonA record {
    string name,
    int age,
};

function emptyRecordForAnyRestField() {
    PersonA p = {name:"John", misc:{}};
}

type Pet record {
    Animal lion;
};

type PersonA record {
    string fname,
    string lname,
    int age,
    string ...
};

type PersonB record {
    string fname,
    string lname,
    int age,
    string   ...
};

type PersonC record {
    string fname,
    string lname,
    int age,
    any
    ...
};
