type Person record {
    string name;
    int age;
};

type Foo record {
    map<string> m;
    int age;
};

type GlobalTable1 table<Person> key(name);
type GlobalTable2 table<Foo> key(m);


GlobalTable1 tab1 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

function testGlobalTableConstructExpr1() returns string {
    return tab1.toString();
}

function testTableConstructExprWithDuplicateKeys() returns string {
    GlobalTable2 tab2 = table [
      { m: {"AAA":"DDDD"}, age: 31 },
      { m: {"AAA":"DDDD"}, age: 34 }
    ];

    return tab2.toString();
}