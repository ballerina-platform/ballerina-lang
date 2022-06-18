type Person record {|
    string name;
    int age;
|};
type TableRec record {|
    table<Person> personTable;
|};
TableRec  t = {person};
