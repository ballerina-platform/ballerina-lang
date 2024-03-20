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

type Zero 0;

type NaturalNums 1|2|3;

type WholeNums Zero|NaturalNums;

function testTableConstructorWithVar6() {
    WholeNums f = 2;

    var v1 = table [{a: f}];
    int _ = v1;
}

type CustomerEmptyKeyedTbl table<Customer> key();

function testInvalidMemberAccessWithEmptyKeyedKeylessTbl() {
    table<Customer> key() tbl1 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    _ = tbl1[222];

    CustomerEmptyKeyedTbl tbl2 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    _ = tbl2[222];

    table<record {|string name?;|}> key() tbl3 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    _ = tbl3["Anne"];

    table<record {string name?;}> key() tbl4 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    _ = tbl4["Anne"];

    table<Customer> key() & readonly tbl5 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    _ = tbl5[222];

    table<record {|string name?;|}> key() & readonly tbl6 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    _ = tbl6["Anne"];

    table<User|Customer> key() tbl7 = table key(id) [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, name: "Anne", address: "LA"}
        ];
    _ = tbl7["Anne"];
}

function testUpdatingMemberWithEmptyKeyedKeylessTbl() {
    table<Customer> key() tbl1 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    tbl1[222] = {id: 222, firstName: "Melina", lastName: "Kodel"};

    CustomerEmptyKeyedTbl tbl2 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    tbl2[222] = {id: 222, firstName: "Melina", lastName: "Kodel"};

    table<record {|string name?;|}> key() tbl3 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    tbl3["Anne"] = {name: "Annie"};

    table<record {string name?;}> key() tbl4 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    tbl4["Anne"] = {name: "Annie"};

    table<Customer> key() & readonly tbl5 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    tbl5[222] = {id: 222, firstName: "Melina", lastName: "Kodel"};

    table<record {|string name?;|}> key() & readonly tbl6 = table [
            {name: "John"},
            {name: "Anne"}
        ];
    tbl6["Anne"] = {name: "Annie"};

    table<User|Customer> key() tbl7 = table key(id) [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, name: "Anne", address: "LA"}
        ];
    tbl7[111] = {id: 111, name: "Annie", address: "LA"};
}

function testIncompatibleTableTypesWithEmptyKeyedKeylessTbl() {
    CustomerEmptyKeyedTbl tbl1 = table [
            {id: 222, firstName: "John", lastName: "Reed"}
        ];

    table<record {|int id; string firstName; string lastName;|}> key() _ = tbl1;

    CustomerTable _ = tbl1;
}

function testAssigningKeyedToEmptyKeyedTbl() {
    table<Customer> key(id) tbl1 = table [
            {id: 222, firstName: "John", lastName: "Reed"},
            {id: 111, firstName: "Anne", lastName: "Frank"}
        ];
    CustomerEmptyKeyedTbl tbl2 = tbl1;
    _ = tbl2[111];
    tbl2[222] = {id: 222, firstName: "Melina", lastName: "Kodel"};
}

type Student record {|
    readonly int id;
    string firstName;
|};

function testIncompatibleTableTypeInAUnion() {
    string lastName = "Jayawickrama";
    table<Student>|int t = table key(id) [{id: 1, firstName: "Lochana", lastName}];
}

type Person1 record {
    readonly int id;
    string name;
};

function testMultipleKeys1() {
    table<Person1> key(id) t1 = table [
        {id: 1, name: "a"},
        {id: 2, name: "a"}
    ];

    Person1? _ = t1[1, 2, 3]; // error
}

type Person2 record {
    readonly int id;
    readonly string name;
};

function testMultipleKeys2() {
    table<Person2> key(id) t1 = table [
        {id: 1, name: "a"},
        {id: 2, name: "a"}
    ];

    Person2? _ = t1[1, "2", "3"]; // error

    table<Person2> key(id) t2 = table [
        {id: 1, name: "a"},
        {id: 2, name: "a"}
    ];

    Person2? _ = t2[11, 12, 13, 14]; // error
}

function testMultipleKeys3() {
    table<Person1> key <int> t1 = table key(id) [
        {id: 1, name: "a"},
        {id: 2, name: "a"}
    ];

    Person1? _ = t1[1, 2, 3]; // error
}

type FooRec record {
    readonly int x;
    int y;
};

FooRec spreadField1 = {x: 1002, y: 30};
FooRec spreadField2 = {x: 1003, y: 25};

table<FooRec> key(x) tb1 = table [
    {x: 1001, y: 20},
    {...spreadField1},
    {...spreadField2}
];

table<FooRec> key(x) tb2 = table [
    {...spreadField1},
    {...spreadField2}
];

type BarRec record {
    readonly int x;
    readonly int y;
    readonly string z;
};

int i = 1;
BarRec spreadField3 = {x: 1003, y: 25, z: "a"};
table<BarRec> key(x, y, z) tb3 = table [
    {x: i, y: i, z: "a"},
    {...spreadField3}
];

type Employee2 record {
    readonly int id;
    string name;
};

function testKeyConstraint() {
    table<Employee2> superTable1 = table key(id) [
        {id: 0, name: "a"},
        {id: 1, name: "b"}
    ];
    _ = superTable1.remove(0); // error

    table<Employee2> superTable2 = table [
        {id: 0, name: "a"},
        {id: 1, name: "b"}
    ];
    _ = superTable2.remove(0); // error

    table<Employee2> key<int> keyTable = table key(id) [
        {id: 0, name: "a"},
        {id: 1, name: "b"}
    ];
    _ = keyTable.remove(0);

    table<Employee2> key<never> keylessTable = table [
        {id: 0, name: "a"},
        {id: 1, name: "b"}
    ];
    _ = keylessTable.remove(0); // error

    var keyTable1 = table key(id) [
        {id: 0, name: "a"},
        {id: 1, name: "b"}
    ];
    _ = keyTable1.remove(0);

    var keylessTable1 = table [
            {id: 0, name: "a"},
            {id: 1, name: "b"}
        ];
    _ = keylessTable1.remove(0); // error

    var ids = from var {id} in keylessTable select {id};
    _ = ids.remove(0); // error
}

string strValue = "abc";
type Row6 record {
    readonly string k;
    int value;
};

table<Row6> key(k) tbl5 = table [
    {k: string `A${idValue}C${strValue}`, value: 17}
];

type Row7 record {
    readonly int[] k;
    int value;
};
int[] intArr = [12, 12];

table<Row7> key(k) tbl7 = table [
    {k: [12, 12, intVal], value: 5},
    {k: [intVal, intVal], value: 17},
    {k: [12, ...intArr], value: 17}
];

type Row8 record {
    readonly table<Row6> key(k) k;
    int value;
};
table<Row8> key(k) tbl8 = table [
    {k: table key(k) [{k: strValue, value: 17}], value: 17}
];

type Row9 record {
    readonly map<anydata> k;
    int value;
};
Row9 mapVal = {k: {idValue, "C": [13.5, 24.3]}, value: 17};
table<Row9> key(k) tbl9 = table [
    {k: {"A": strValue, "B": idValue, "C": [13.5, 24.3]}, value: 17},
    {k: {strValue, "B": "idValue", "C": [13.5, 24.3]}, value: 17},
    {k: {[strValue]: "a", "B": "idValue", "C": [13.5, 24.3]}, value: 17},
    {...mapVal}
];

type Row10 record {
    readonly float k;
    int value;
};
table<Row10> key(k) tbl10 = table [
    {k: <float>idValue, value: 17}
];

int? intOrNull = 12;
type Row11 record {
    readonly int k;
    int value;
};
table<Row11> key(k) tbl11 = table [
    {k: +idValue, value: 17},
    {k: idValue * 12, value: 19},
    {k: idValue + 12, value: 11},
    {k: idValue >> 13, value: 11},
    {k: idValue | 13, value: 11},
    {k: idValue & 13, value: 11},
    {k: idValue is int ? 12 : 1, value: 11},
    {k: idValue, value: 11},
    {k: intOrNull ?: 1, value: 11},
    {k: (idValue is int ? (<int> 10.5) : 20), value: 11}
];

type Row12 record {
    readonly boolean k;
    int value;
};
table<Row12> key(k) tbl12 = table [
    {k: 5 < idValue, value: 17},
    {k: idValue is int, value: 19},
    {k: idValue !is int, value: 11},
    {k: idValue == 15, value: 11},
    {k: idValue != 15, value: 11},
    {k: idValue === 15, value: 11},
    {k: idValue !== 15, value: 11},
    {k: idValue == 15 || false, value: 11},
    {k: idValue == 15 && true, value: 11},
    {k: !(idValue == 15 && true), value: 11}
];
