type Person record {
    string name;
    int age;
};

type GlobalTable1 table<Person> key(name);

GlobalTable1 tab1 = table [
  { name: "AAA", age: 31 },
  { name: "BBB", age: 34 }
];

function testGlobalTableConstructExpr() returns boolean {
    tab1["CCC"] =  { name: "CCC", age: 34 };
    return tab1.toString() == "name=AAA age=31\nname=BBB age=34";
}