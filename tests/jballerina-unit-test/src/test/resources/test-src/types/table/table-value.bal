type Person record {
    string name;
    int age;
};

type GlobalTable table<Person> key(name);

GlobalTable tab = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

function testGlobalTableConstructExpr() returns string {
    return tab.toString();
}