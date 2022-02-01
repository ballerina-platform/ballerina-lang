type Customer record {
  readonly int id;
  readonly  string firstName;
  string lastName;
};

type Person record {
    string name;
    readonly int age;
};

type CustomerTable table<Customer> key(id);
type GlobalTable table<Person> key<string>;

CusTable tab1 = table key(id, firstName) [
 { id: 222, firstName: "Sanjiva", lastName: "Weerawarana" },
 { id: 111, firstName: "James", lastName: "Clark" }
];

CustomerTable tab2 = table key(id, firstName) [
 { id: 222, firstName: "Sanjiva", lastName: "Weerawarana" },
 { id: 111, firstName: "James", lastName: "Clark" }
];

GlobalTable tab3 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

GlobalTable tab4 = table key(age) [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

CustomerTable invalidCustomerTable = table key(address) [
 { id: 222, firstName: "Sanjiva", lastName: "Weerawarana" },
 { id: 111, firstName: "James", lastName: "Clark" }
];

var customerTable = table [
 { id: 222, firstName: "Sanjiva", lastName: "Weerawarana" },
 { id: 111, firstName: "James", lastName: "Clark" }
];

Customer customer = customerTable[222];

table<int> tableWithInvalidConstarint = table [];

function testArrayAccessWithMultiKey() returns (string) {
    map<any> namesMap = {fname:"Supun",lname:"Setunga"};
    string keyString = "";
    var a = namesMap["fname","lname"];
    keyString =  a is string ? a : "";
    return keyString;
}

type Teacher record {
    string name;
    int age;
};

type TeacherTable table<Teacher> key(name);

TeacherTable teacheTab = table [
    { name: "AAA", age: 31 },
    { name: "BBB", age: 34 }
    ];

type User record {
    readonly int id;
    readonly string name?;
    string address;
};

type UserTable table<User> key(id, name);

UserTable userTab = table [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                                   { id: 45 , name: "James" , address: "Clark" }];

int idValue = 15;
table<Customer> key(id) cusTable = table [{ id: 13 , firstName: "Sanjiva", lastName: "Weerawarana"},
                                        {id: idValue, firstName: "James" , lastName: "Clark"}];

table<Customer> keylessCusTab  = table [{ id: 222, firstName: "Sanjiva", lastName: "Weerawarana" },
                                    { id: 111, firstName: "James", lastName: "Clark" }];

Customer customerRecord = keylessCusTab[222];

var invalidCusTable = table key(id) [{ id: 13 , firstName: "Sanjiva", lastName: "Weerawarana"},
                                {id: idValue, firstName: "James" , lastName: "Clark"}];

table<Customer> key<int> intKeyConstraintTable = table key(id)[{ id: 13 , firstName: "Sanjiva", lastName: "Weerawarana" },
                                                { id: 23 , firstName: "James" , lastName: "Clark" }];

table<Customer> key<string> stringKeyConstraintTable = intKeyConstraintTable;

type UserCustTable table<User|Customer> key<int>;

type UserInfoTable table<User> key<int> | table<Customer> key<int>;

function testInvalidKeyForInferTypeTable() {
    var tab = table key(no) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" },
                                       { id: 133 , name: "Mohan", lname: "Darshan" , address: "Colombo"} ];
}

function testRequiredFieldForInferTypeTable() {
    var tab = table key(address) [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                        { id: 23 , name: "James" },
                                       { id: 133 , name: "Mohan", lname: "Darshan" , address: "Colombo"} ];
}

function testMapConstraintTableWithKeySpecifier() {
    table<map<any>> key(lname) tab = table key() [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                            { id: 23 , name: "James" },
                                           { id: 133 , name: "Mohan", lname: "JJ" , address: "Colombo"} ];
}

function testMemberAccessMapConstraintTable() {
    table<map<any>> key(id) tab = table key() [{ id: 13 , name: "Sanjiva", lname: "Weerawarana" },
                                            { id: 23 , name: "James" },
                                           { id: 133 , name: "Mohan", lname: "JJ" , address: "Colombo"} ];

    map<any> mapObject = tab[13];
}

function testVarTypeTableInfering() {
    var customerTable = table [];
    customerTable.put({id: 3, name: "Pope", age: 19, address: {no: 12, road: "Sea street"}});
}

function testMemberAccessWithInvalidStaticType() {
    table<Customer> key(id) customerTable = table [{ id: 13 , firstName: "Sanjiva", lastName: "Weerawarana" },
                                        { id: 23 , firstName: "James" , lastName: "Clark" }];
    Customer customer = customerTable[18];
}

function testMemberAccessWithInvalidStaticType2() {
    UserCustTable userTab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                                   { id: 45 , name: "James" , address: "Clark" }];
    User user = userTab[18];
}

function testMemberAccessWithInvalidStaticType3() {
    UserInfoTable infoTab = table key(id) [{ id: 13 , name: "Sanjiva", address: "Weerawarana" },
                                                   { id: 45 , name: "James" , address: "Clark" }];
    table<Customer> key(id) tab = <table<Customer> key(id)> infoTab;
    Customer customer = tab[18];
}

type Employee record {
    string name;
    readonly int age;
};

type EmployeeTable table<Employee> key(name);

class A {

}

readonly & A[] a = [];

type Row record {
    readonly A[] k;
    int value;
};

table<Row> key(k) t = table [
    { k: a, value: 17, "b":12}
];

type Row2 record {
    readonly int k;
    readonly string[] m;
    int value;
};

int intVal = 12;

table<Row2> key(k) t2 = table [
    { k: intVal, m: ["foo", "bar"], value: 17, "b":12}
];

readonly & string[] stringArr = ["foo", "bar"];

table<Row2> key(k, m) t3 = table [
    { k: 20, m: stringArr, value: 17, "b":12}
];

function someFunc(table<any> p) {
}

type T table<any>;

function testInvalidTableKeys() {
    table<Person> key(id, name) tableVar;
    table<Person> key(invalidField) tableVar2;
}

type EmployeeId record {
    readonly string firstname;
    readonly string lastname;
};

type Employee1 record {
    *EmployeeId;
    readonly int leaves;
};

table<Employee1> key<EmployeeId> tbl1 = table key(leaves) [{firstname: "John", lastname: "Wick", leaves: 10}];

table<Employee1> key<[string, string]> tbl2 = table key(firstname) [{firstname: "John", lastname: "Wick", leaves: 10}];

table<Employee1> key<EmployeeId> tbl3 = table key(firstname) [{firstname: "John", lastname: "Wick", leaves: 10}];

type CustomerDetail record {
    readonly Name name;
    readonly int id;
    string address;
};

type Name record {
    string fname;
    string lname;
};

type CustomerTableWithKTC table<CustomerDetail> key<Name>;

CustomerTableWithKTC tbl4 = table key(firstname, lastname) [{name: {fname: "Sanjiva", lname: "Weerawarana"},
                id: 13, address: "Sri Lanka"}];

function variableNameFieldAsKeyField() {
    int id = 1;

    table<record {readonly int id; string name;}> key (id) _ = table [
        {id, name: "Jo"},
        {id: 1, name: "Amy"},
        {id: 2, name: "Amy"},
        {id, name: "Alex"}
    ];
}

function testTableConstructorWithVar1() {
    string s1 = "id";
    string s2 = "employed";

    var v1 = table [
            {name: "Jo"},
            {[s1] : 2},
            {[s2] : false}
        ];

    table<record {|string name?;|}> _ = v1;

    map<int> m = {name: 1, b: 2};

    var v2 = table [
        {name: "Jo"},
        {...m}
    ];

    table<record {|string name?;|}> _ = v2;
    table<record {|string|int name?;|}> _ = v2;
}

type FooUnion int|string;

function testTableConstructorWithVar2() {
    FooUnion f = 1;

    var v1 = table [
        {a: f},
        {a: 1}
    ];
    int _ = v1;
}

type FooRec2 record {|
    int i;
    never j?;
    never k?;
    never...;
|};

function testTableConstructorWithVar3(FooRec2 f) {
    var v1 = table [
            {...f},
            {i: 1, j: 2, l: ""}
        ];
    int _ = v1;
}

function testTableConstructorWithVar4() {
    anydata|error f = 1;

    var v1 = table [
            {a: f},
            {a: 1}
        ];
    int _ = v1;
}

function testTableConstructorWithVar5() {
    any|error f = 1;

    var v1 = table [
            {a: f},
            {a: 1}
        ];
    int _ = v1;
}
