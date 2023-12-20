type Person record {|
    readonly int id;
    string name;
|};

function foo() {
    Person p = { id: 1, name: "foo" };
    table<Person> tHuman = table [{id: 1, name: "Jane"}];
    stream<Person> s = new;
}
